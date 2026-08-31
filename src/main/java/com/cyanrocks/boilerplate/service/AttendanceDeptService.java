package com.cyanrocks.boilerplate.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyanrocks.boilerplate.dao.entity.AttendanceDept;
import com.cyanrocks.boilerplate.dao.entity.AttendanceUser;
import com.cyanrocks.boilerplate.dao.mapper.AttendanceDeptMapper;
import com.cyanrocks.boilerplate.dao.mapper.AttendanceUserMapper;
import com.cyanrocks.boilerplate.utils.DingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部门架构业务类（工时看板 支线逻辑2）
 * 从根部门(1)递归拉取钉钉部门树，落库 attendance_dept 表，
 * 并通过 /topapi/user/listid 获取每个部门的直属员工，回填考勤人员的所属部门
 * @Author yangshihao
 * @Date 2026/8/20
 */
@Service
public class AttendanceDeptService {

    private static final Logger LOG = LoggerFactory.getLogger(AttendanceDeptService.class);

    /** 根部门ID */
    private static final Long ROOT_DEPT_ID = 1L;
    /** 部门树递归深度上限（防御性） */
    private static final int MAX_DEPTH = 10;

    /** 部门信息（ID+名称） */
    private static class DeptInfo {
        Long deptId;
        String deptName;
    }

    @Autowired
    private AttendanceDeptMapper attendanceDeptMapper;
    @Autowired
    private AttendanceUserMapper attendanceUserMapper;
    @Autowired
    private DingUtils dingUtils;

    /**
     * 同步部门树 + 考勤人员所属部门
     * 任意接口调用失败时直接抛异常中止（不执行回填），避免用残缺数据把考勤人员的部门信息清空
     * @return 同步的部门数
     */
    public int syncDepartments() {
        // 1. 递归拉取部门树并落库，同时收集 userId -> 部门 映射
        Map<String, DeptInfo> userDeptMap = new HashMap<>();
        int deptCount = syncDeptRecursive(ROOT_DEPT_ID, userDeptMap, 0);
        LOG.info("部门树同步完成: 部门数={}, 获取到人员部门映射数={}", deptCount, userDeptMap.size());

        // 2. 回填考勤人员的所属部门（只更新 attendance_user 表中的人员）
        List<AttendanceUser> attendanceUsers = attendanceUserMapper.selectList(null);
        int updateCount = 0;
        LocalDateTime now = LocalDateTime.now();
        for (AttendanceUser attendanceUser : attendanceUsers) {
            DeptInfo dept = userDeptMap.get(attendanceUser.getDingUserId());
            Long newDeptId = (dept != null) ? dept.deptId : null;
            String newDeptName = (dept != null) ? dept.deptName : null;
            // 部门无变化不更新
            boolean changed = (newDeptId == null && attendanceUser.getDeptId() != null)
                    || (newDeptId != null && !newDeptId.equals(attendanceUser.getDeptId()));
            if (!changed) {
                continue;
            }
            attendanceUser.setDeptId(newDeptId);
            attendanceUser.setDeptName(newDeptName);
            attendanceUser.setUpdateTime(now);
            attendanceUserMapper.updateById(attendanceUser);
            updateCount++;
        }
        LOG.info("考勤人员部门回填完成: 考勤人员数={}, 更新={}", attendanceUsers.size(), updateCount);
        return deptCount;
    }

    /**
     * 递归同步部门树（广度优先逐层下钻）
     * 任意钉钉接口调用失败（返回null）时抛异常中止整个同步，防止子树缺失导致回填时清空考勤人员的部门信息
     * @param parentDeptId 父部门ID
     * @param userDeptMap 输出：userId -> 部门 映射
     * @param depth 递归深度
     * @return 本次递归处理的部门数
     */
    private int syncDeptRecursive(Long parentDeptId, Map<String, DeptInfo> userDeptMap, int depth) {
        if (depth > MAX_DEPTH) {
            LOG.warn("部门树递归深度超过上限，停止: parentDeptId={}, depth={}", parentDeptId, depth);
            return 0;
        }
        List<JSONObject> subDepts = dingUtils.getSubDepartments(parentDeptId);
        if (null == subDepts) {
            throw new IllegalStateException("获取子部门列表失败, 同步中止: parentDeptId=" + parentDeptId);
        }
        if (CollectionUtil.isEmpty(subDepts)) {
            return 0;
        }
        int count = 0;
        LocalDateTime now = LocalDateTime.now();
        for (JSONObject subDept : subDepts) {
            Long deptId = subDept.getLong("dept_id");
            String deptName = subDept.getStr("name");
            Long parentId = subDept.getLong("parent_id");
            if (deptId == null) {
                continue;
            }
            // 部门upsert
            AttendanceDept entity = attendanceDeptMapper.selectOne(
                    Wrappers.<AttendanceDept>lambdaQuery().eq(AttendanceDept::getDeptId, deptId));
            boolean isInsert = (entity == null);
            if (isInsert) {
                entity = new AttendanceDept();
                entity.setDeptId(deptId);
                entity.setCreateTime(now);
            }
            entity.setDeptName(deptName);
            entity.setParentId(parentId);
            entity.setUpdateTime(now);
            if (isInsert) {
                attendanceDeptMapper.insert(entity);
            } else {
                attendanceDeptMapper.updateById(entity);
            }
            count++;

            // 获取该部门直属员工（不含子部门员工，由递归的下一层获取）
            List<String> userIds = dingUtils.getDeptUserIds(deptId);
            if (null == userIds) {
                throw new IllegalStateException("获取部门员工列表失败, 同步中止: deptId=" + deptId + ", deptName=" + deptName);
            }
            for (String userId : userIds) {
                // 钉钉用户可属于多个部门，保留第一次遇到的（上层优先）
                userDeptMap.putIfAbsent(userId, buildDeptInfo(deptId, deptName));
            }
            // 递归子部门
            count += syncDeptRecursive(deptId, userDeptMap, depth + 1);
        }
        return count;
    }

    private DeptInfo buildDeptInfo(Long deptId, String deptName) {
        DeptInfo info = new DeptInfo();
        info.deptId = deptId;
        info.deptName = deptName;
        return info;
    }

    /**
     * 查询部门组织架构树（嵌套children结构）
     * 顶层为根部门(1)的直属子部门
     */
    public List<Map<String, Object>> listDeptTree() {
        List<AttendanceDept> all = attendanceDeptMapper.selectList(
                Wrappers.<AttendanceDept>lambdaQuery().orderByAsc(AttendanceDept::getDeptId));
        // 按父部门ID分组
        Map<Long, List<AttendanceDept>> childrenMap = new HashMap<>();
        for (AttendanceDept dept : all) {
            Long parentKey = (dept.getParentId() == null) ? ROOT_DEPT_ID : dept.getParentId();
            childrenMap.computeIfAbsent(parentKey, k -> new ArrayList<>()).add(dept);
        }
        // 挂到父部门ID为1的节点下
        List<Map<String, Object>> tree = buildChildren(ROOT_DEPT_ID, childrenMap);
        // 兜底：父部门不在库中的孤儿节点挂到顶层，避免丢失
        java.util.Set<Long> knownDeptIds = new java.util.HashSet<>();
        for (AttendanceDept dept : all) {
            knownDeptIds.add(dept.getDeptId());
        }
        for (Map.Entry<Long, List<AttendanceDept>> entry : childrenMap.entrySet()) {
            if (entry.getKey().equals(ROOT_DEPT_ID) || knownDeptIds.contains(entry.getKey())) {
                continue;
            }
            for (AttendanceDept orphan : entry.getValue()) {
                LOG.warn("部门父节点不在库中，挂到顶层: deptId={}, deptName={}, parentId={}",
                        orphan.getDeptId(), orphan.getDeptName(), orphan.getParentId());
                tree.add(buildNode(orphan, childrenMap));
            }
        }
        return tree;
    }

    /**
     * 递归构建子部门节点列表
     */
    private List<Map<String, Object>> buildChildren(Long parentId, Map<Long, List<AttendanceDept>> childrenMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (AttendanceDept dept : childrenMap.getOrDefault(parentId, new ArrayList<>())) {
            result.add(buildNode(dept, childrenMap));
        }
        return result;
    }

    /**
     * 构建单个部门节点（含递归children）
     */
    private Map<String, Object> buildNode(AttendanceDept dept, Map<Long, List<AttendanceDept>> childrenMap) {
        Map<String, Object> node = new HashMap<>();
        node.put("deptId", dept.getDeptId());
        node.put("deptName", dept.getDeptName());
        node.put("parentId", dept.getParentId());
        node.put("children", buildChildren(dept.getDeptId(), childrenMap));
        return node;
    }
}

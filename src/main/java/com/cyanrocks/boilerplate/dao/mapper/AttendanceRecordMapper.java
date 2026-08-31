package com.cyanrocks.boilerplate.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyanrocks.boilerplate.dao.entity.AttendanceRecord;
import com.cyanrocks.boilerplate.vo.dto.AttendanceDailySummaryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecord> {

    /**
     * 每日工时汇总分页查询（按工作日，上下班都有的记录）
     */
    @Select("<script>"
            + "SELECT r.ding_user_id,"
            + "       COALESCE(u.username, au.other_name)                            AS username,"
            + "       DATE(r.work_date)                                                AS work_day,"
            + "       MAX(CASE WHEN r.check_type = 'OnDuty' THEN r.user_check_time END)  AS on_duty_time,"
            + "       MAX(CASE WHEN r.check_type = 'OffDuty' THEN r.user_check_time END) AS off_duty_time,"
            + "       TIMESTAMPDIFF(SECOND,"
            + "                     MAX(CASE WHEN r.check_type = 'OnDuty' THEN r.user_check_time END),"
            + "                     MAX(CASE WHEN r.check_type = 'OffDuty' THEN r.user_check_time END)) AS duration_seconds,"
            + "       ROUND(TIMESTAMPDIFF(SECOND,"
            + "                           MAX(CASE WHEN r.check_type = 'OnDuty' THEN r.user_check_time END),"
            + "                           MAX(CASE WHEN r.check_type = 'OffDuty' THEN r.user_check_time END)) / 3600, 2) AS duration_hours"
            + " FROM attendance_record r"
            + " LEFT JOIN (SELECT ding_id, MAX(username) AS username FROM user GROUP BY ding_id) u ON u.ding_id = r.ding_user_id"
            + " LEFT JOIN attendance_user au ON au.ding_user_id = r.ding_user_id"
            + " WHERE r.check_type IN ('OnDuty', 'OffDuty')"
            + "<if test='startTime != null'> AND r.work_date &gt;= #{startTime}</if>"
            + "<if test='endTime != null'> AND r.work_date &lt; #{endTime}</if>"
            + " GROUP BY r.ding_user_id, DATE(r.work_date)"
            + " HAVING MAX(CASE WHEN r.check_type = 'OnDuty' THEN r.user_check_time END) IS NOT NULL"
            + "    AND MAX(CASE WHEN r.check_type = 'OffDuty' THEN r.user_check_time END) IS NOT NULL"
            + " ORDER BY work_day, r.ding_user_id"
            + " LIMIT #{offset}, #{size}"
            + "</script>")
    List<AttendanceDailySummaryDTO> selectDailySummary(@Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime enfadTime,
                                                       @Param("offset") int offset,
                                                       @Param("size") int size);

    /**
     * 每日工时汇总总条数
     */
    @Select("<script>"
            + "SELECT COUNT(*) FROM ("
            + "  SELECT r.ding_user_id, DATE(r.work_date) AS work_day"
            + "  FROM attendance_record r"
            + "  WHERE r.check_type IN ('OnDuty', 'OffDuty')"
            + "<if test='startTime != null'> AND r.work_date &gt;= #{startTime}</if>"
            + "<if test='endTime != null'> AND r.work_date &lt; #{endTime}</if>"
            + "  GROUP BY r.ding_user_id, DATE(r.work_date)"
            + "  HAVING MAX(CASE WHEN r.check_type = 'OnDuty' THEN r.user_check_time END) IS NOT NULL"
            + "     AND MAX(CASE WHEN r.check_type = 'OffDuty' THEN r.user_check_time END) IS NOT NULL"
            + ") t"
            + "</script>")
    long countDailySummary(@Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime);
}

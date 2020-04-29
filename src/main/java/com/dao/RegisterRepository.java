package com.dao;

import com.bean.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RegisterRepository {
    @Autowired
    private JdbcTemplate template;
    private RegisterRowMapper registerRowMapper = new RegisterRowMapper();
    private StudentRowMapper studentRowMapper = new StudentRowMapper();

    //增
    public boolean insertANewRegister(int reg_teacherId, int reg_studentId, int reg_courseId) {
        try {
            template.update("insert into courseRegister(reg_teacherId,reg_studentId,reg_courseId) values(?,?,?)", reg_teacherId, reg_studentId, reg_courseId);
            return true;
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    public boolean insertCompulsory(Course comCourse, Teacher comTeacher) {
        try {
            List<Student> students = template.query("select * from Student where studentDepartId=?", studentRowMapper, comCourse.getCourseDepartId());
            for (Student student : students) {
                template.update("insert into courseRegister(reg_teacherId,reg_studentId,reg_courseId) values(?,?,?)", comTeacher.getTeacherId(), student.getStudentId(), comCourse.getCourseId());
            }
            return true;
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    //删
    public boolean deleteRegister(int reg_studentId, int reg_courseId) {
        try {
            template.update("delete from courseRegister where reg_studentId=? and reg_courseId=?", reg_studentId, reg_courseId);
            return true;
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    //改
    public boolean updateGrade(int courseId, int studentId, float grade, float testScore, float finalScore) {
        try {
            template.update("update courseRegister set grade=? , testScore=? , finalScore=? where reg_courseId=? and reg_studentId=?", grade, testScore, finalScore, courseId, studentId);
            return true;
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    //查
    public List<Register> selectRegisterByCourseId(int reg_courseId) {
        try {
            List<Register> registers = template.query("select * from Course,Teacher,Student,courseRegister " +
                    "where courseId=reg_courseId " +
                    "and teacherId=reg_teacherId " +
                    "and studentId=reg_studentId " +
                    "and courseId=?", registerRowMapper, reg_courseId);
            return registers;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public List<Register> selectRegisterByStudentId(int studentId) {
        try {
            List<Register> registers = template.query("select * from Course,Student,Teacher,courseRegister " +
                    "where reg_studentId=studentId " +
                    "and reg_courseId=courseId " +
                    "and reg_teacherId=teacherId " +
                    "and courseRegister.isEnable='T' " +
                    "and studentId=?", registerRowMapper, studentId);
            return registers;

        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public List<Register> selectRegisters(String order_by, String order) {
        try {

            String sql = "select * from Course,Teacher,Student,courseRegister " +
                    "where reg_courseId=courseId " +
                    "and reg_teacherId=teacherId " +
                    "and reg_studentId=studentId order by " + order_by;
            if ("0".equals(order))
                sql += " desc";
            List<Register> registers = template.query(sql, registerRowMapper);
            return registers;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }


}

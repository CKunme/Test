package com.service;

import com.bean.Register;
import com.dao.RegisterRepository;
import com.dao.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegisterService {
    @Autowired
    private RegisterRepository registerRepository;

    public boolean addNewRegister(int reg_teacherId,int reg_studentId,int reg_courseId){
        return registerRepository.insertANewRegister(reg_teacherId,reg_studentId,reg_courseId);
    }

    public boolean removeRegister(int reg_studentId,int reg_courseId){
        return registerRepository.deleteRegister(reg_studentId,reg_courseId);
    }

    public boolean giveGrade(int courseId,int studentId,float grade,float testScore,float finalScore){
        return registerRepository.updateGrade(courseId, studentId, grade, testScore, finalScore);
    }

    public Register getRegisterByCourseId(int reg_courseId){
        return registerRepository.selectScheduleByCourseId(reg_courseId);
    }

    public List<Register> getMyRegister(int studentId){
        return registerRepository.selectScheduleByStudentId(studentId);
    }

    public List<Register> getRegisters(String order_by,String order){
        return registerRepository.selectRegisters(order_by,order);
    }

}

package com.controller;

import com.bean.Course;

import com.bean.Schedule;
import com.service.CourseService;
import com.service.DepartService;
import com.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
public class CourseController {
    @Autowired
    private CourseService courseService;
    @Autowired
    private DepartService departService;
    @Autowired
    private TopicService topicService;

    //测试用的，整合的时候要删掉
    @GetMapping("/Index")
    public ModelAndView index() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("index");
        return mav;
    }

    //课程详细页

    @GetMapping("Course/detail/{courseId}")
    public ModelAndView detailPage(@PathVariable("courseId")int courseId){
        ModelAndView mav=new ModelAndView("detail");
        mav.addObject("courseMsg",courseService.getCourseById(courseId));
        mav.addObject("logo",courseService.getCourseById(courseId).getCourseLogo());
        return mav;
    }


    //新闻详细页
    @GetMapping("News/news/{newsid}")
    public ModelAndView news(@PathVariable("newsid") int newsid)
    {
        ModelAndView mav=new ModelAndView("newsDetail");
        mav.addObject("newsid",newsid);
        return mav;
    }

    @GetMapping("/courseAttribute")
    public ModelAndView courseAttribute(){ModelAndView mav = new ModelAndView("courseAttribute"); return mav;}


    //增

    //此处为新建课程，需要课程字段：name，topicName，departName，Period，Credit，StartTime
    //Logo（courseImg），Level，Type。
    @PostMapping("Course/newCourse")
    public String addNewCourse(@RequestParam("courseImg") MultipartFile courseImg, @ModelAttribute(value = "newCourse") Course newCourse) {
        String msg = "";
        //设置课程类型以及学院的id。
        if (departService.getDepartmentByName(newCourse.getCourseDepartName()) == null) {
            msg = "学院不存在，请先新建学院";
            return msg;
        } else if (topicService.getTopicByName(newCourse.getCourseTopicName()) == null) {
            topicService.addNewTopic(newCourse.getCourseTopicName());
            msg = "课程类型不存在，已自动新建课程类型！";
        }

        newCourse.setCourseTopicId(topicService.getTopicByName(newCourse.getCourseTopicName()).getTopicId());
        newCourse.setCourseDepartId(departService.getDepartmentByName(newCourse.getCourseDepartName()).getDepartId());

        //判断是否传入图片。
        if (courseImg.isEmpty()) {
            msg = "请上传图片！";
            return msg;
        }
        //设置imgName。
        String imgName = System.currentTimeMillis() + courseImg.getOriginalFilename();
        //获取课程图片存储文件夹，若不存在，就创建文件夹。
        String fileDirPath = "C:\\Program Files\\Tomcat 9.0\\webapps\\demo-0.0.1-SNAPSHOT\\WEB-INF\\classes\\static\\img\\courseImg";
        File fileDir = new File(fileDirPath);
        if (!fileDir.exists()) {
            // 递归生成文件夹
            fileDir.mkdirs();
        }
        try {
            // 构建真实的文件路径
            File newFile = new File(fileDir.getAbsolutePath() + File.separator + imgName);
            //输出文件路径。
            //System.out.println(newFile.getAbsolutePath());
            // 上传图片到 -》 “绝对路径”
            courseImg.transferTo(newFile);
            //System.out.println("上传成功！");
            //设置课程图片Logo。
            newCourse.setCourseLogo(newFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //至此，输入的有：name，topicName，departName，Period，Credit，StartTime，Level，Type
        //获取的有：topicId，departId，Logo
        //共11个字段。
        //插入课程。
        if (courseService.addNewCourse(newCourse))
            msg += "课程新建成功！";
        else
            msg += "课程新建失败！";
        return msg;
    }

    //删
    //删除课程
    @PostMapping("Course/removeCourse")
    public boolean removeCourse(@RequestBody List<Integer> courseIds) {
        try {
            for (int courseId : courseIds) {
                Course course_drop = courseService.getCourseById(courseId);
                if (course_drop != null) {
                    File file = new File(course_drop.getCourseLogo());
                    if (file.exists() && file.isFile()) {
                        file.delete();
                    }
                    courseService.removeCourse(courseId);
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }


    //改

    //下架课程
    @PostMapping("Course/dropCourse")
    public boolean dropCourse(@RequestBody List<Integer> courseIds) {
        for (int courseId : courseIds) {
            if (courseService.getCourseById(courseId) == null || !courseService.dropCourse(courseId)) {
                return false;
            }
        }
        return true;
    }

    //上架课程
    @PostMapping("Course/restoreCourse")
    public boolean restoreCourse(@RequestBody List<Integer> courseIds) {
        for (int courseId : courseIds) {
            if (courseService.getCourseById(courseId) == null || !courseService.restoreCourse(courseId)) {
                return false;
            }
        }
        return true;
    }

    //修改课程的全部信息
    @PostMapping("/Course/modifyCourse")
    public String modifyCourse(@RequestParam("courseImg") MultipartFile courseImg, @ModelAttribute(value = "modifyCourse") Course modifyCourse) {
        String msg = "";
        Course oldCourse = courseService.getCourseById(modifyCourse.getCourseId());
        if (oldCourse != null) {
            File file = new File(oldCourse.getCourseLogo());
            if (file.exists() && file.isFile()) {
                file.delete();
            } else {
                msg = "旧的课程图片不存在！";
            }
            //判断修改的新的课程信息是否符合要求
            if (departService.getDepartmentByName(modifyCourse.getCourseDepartName()) == null) {
                msg = "学院不存在，请先新建学院";
                return msg;
            } else if (topicService.getTopicByName(modifyCourse.getCourseTopicName()) == null) {
                topicService.addNewTopic(modifyCourse.getCourseTopicName());
                msg = "新的课程类型不存在，已自动新建课程类型！";
            }
            //设置新的课程topic和department信息
            modifyCourse.setCourseTopicId(topicService.getTopicByName(modifyCourse.getCourseTopicName()).getTopicId());
            modifyCourse.setCourseDepartId(departService.getDepartmentByName(modifyCourse.getCourseDepartName()).getDepartId());
            //设置imgName。
            String imgName = System.currentTimeMillis() + courseImg.getOriginalFilename();
            //获取课程图片存储文件夹，若不存在，就创建文件夹。
            String fileDirPath = "C:\\Program Files\\Tomcat 9.0\\webapps\\demo-0.0.1-SNAPSHOT\\WEB-INF\\classes\\static\\img\\courseImg";
            File fileDir = new File(fileDirPath);
            try {
                // 构建真实的文件路径
                File newFile = new File(fileDir.getAbsolutePath() + File.separator + imgName);
                //输出文件路径。
                //System.out.println(newFile.getAbsolutePath());
                // 上传图片到 -》 “绝对路径”
                courseImg.transferTo(newFile);
                //System.out.println("上传成功！");
                //设置课程图片Logo。
                modifyCourse.setCourseLogo(newFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (courseService.modifyCourse(modifyCourse))
                msg += "课程修改成功！";
            else
                msg += "课程修改失败！";
            return msg;
        }
        return "课程修改失败！";
    }

    @GetMapping("/Course/list/{Param}/{value}")
    public ModelAndView list(@PathVariable("Param") String Param, @PathVariable("value") String value) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("list");
        mav.addObject("Param",Param);
        mav.addObject("value",value);
        return mav;
    }
    //修改课程的部分信息
    //传入一个课程对象
    //需要课程字段：Id，Description，FAQ，gradingPolicy，Requirement
    @PostMapping("Course/modifyCourseInfo")
    public boolean modifyCourseInfo(@ModelAttribute(value = "courseInfo") Course courseInfo) {
        return courseService.modifyCourseInfo(courseInfo);
    }

    //查
    @GetMapping("Course/courseById/{courseId}")
    public Course getCourseById(@PathVariable("courseId") int courseId) {
        return courseService.getCourseById(courseId);
    }

    // order_by表示根据哪个字段排序
    // order表示正序还是倒序查询，order为0表示逆序，1表示正序
    // isEnable表示是否启用，on表示查询启用的课程，off表示查询未启用的课程，all表示查询所有
    // haveTeacher表示查询是否有老师的课程，have表示查询有老师的课程，lack表示查询没有老师的课程，all表示查询所有
    // page表示第几页，pageSize表示每页几条数据
    // 若page和pageSize都为0，则返回所有数据。
    // 示例：Course/courses/on/have/courseId/0
    @GetMapping("Course/courses/{isEnable}/{haveTeacher}/{order_by}/{order}/{page}/{pageSize}")
    public List<Course> getCourses(@PathVariable("isEnable") String isEnable, @PathVariable("haveTeacher") String haveTeacher, @PathVariable("order_by") String order_by, @PathVariable("order") String order, @PathVariable("page") int page, @PathVariable("pageSize") int pageSize) {
        return courseService.getCourses(isEnable, haveTeacher, order_by, order, page, pageSize);
    }






    //查询功能！！！
    //根据特定字段查询，查询范围：course
    @GetMapping("SearchCo/{param}/{value}")
    public List<Course> searchCourse(@PathVariable("param") String param, @PathVariable("value") String value){
        return courseService.getAll(param, value);
    }
}


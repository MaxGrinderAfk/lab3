package idespring.lab3.controller.studentcontroller;

import idespring.lab3.model.Student;
import idespring.lab3.service.studservice.StudentServ;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
public class StudentController {
    private final StudentServ studentService;

    @Autowired
    public StudentController(StudentServ studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@Valid @RequestBody Student student) {
        return new ResponseEntity<>(studentService.addStudent(student), HttpStatus.CREATED);
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<Student> getStudentById(@Positive @NotNull @PathVariable Long studentId) {
        try {
            Student student = studentService.findById(studentId);
            return new ResponseEntity<>(student, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<Student>> getStudents(
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Long id) {
        List<Student> students = studentService.readStudents(age, sort, id);
        return !students.isEmpty()
                ? new ResponseEntity<>(students, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Student>> getStudentsByGroup(
            @Positive @NotNull @PathVariable Long groupId) {
        List<Student> students = studentService.findByGroupId(groupId);
        return !students.isEmpty()
                ? new ResponseEntity<>(students, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{studentId}")
    public ResponseEntity<Student> updateStudent(
            @Positive @NotNull @PathVariable Long studentId,
            @RequestParam(required = false, defaultValue = "unknown") String name,
            @Positive @RequestParam(required = false, defaultValue = "15") int age) {
        try {
            studentService.updateStudent(name, age, studentId);
            Student updatedStudent = studentService.findById(studentId);
            return new ResponseEntity<>(updatedStudent, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> deleteStudent(@Positive @NotNull @PathVariable Long studentId) {
        try {
            studentService.deleteStudent(studentId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
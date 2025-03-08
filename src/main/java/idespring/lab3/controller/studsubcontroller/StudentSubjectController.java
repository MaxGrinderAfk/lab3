package idespring.lab3.controller.studsubcontroller;

import idespring.lab3.model.Student;
import idespring.lab3.model.Subject;
import idespring.lab3.service.studentsubjserv.StudentSubjectService;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student-subjects")
public class StudentSubjectController {
    private final StudentSubjectService studentSubjectService;

    @Autowired
    public StudentSubjectController(StudentSubjectService studentSubjectService) {
        this.studentSubjectService = studentSubjectService;
    }

    @PostMapping
    public ResponseEntity<Void> addSubjectToStudent(
            @RequestParam Long studentId,
            @RequestParam Long subjectId) {
        try {
            studentSubjectService.addSubjectToStudent(studentId, subjectId);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> removeSubjectFromStudent(
            @RequestParam Long studentId,
            @RequestParam Long subjectId) {
        try {
            studentSubjectService.removeSubjectFromStudent(studentId, subjectId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{studentId}/subjects")
    public ResponseEntity<Set<Subject>> getSubjectsByStudent(@PathVariable Long studentId) {
        Set<Subject> subjects = new HashSet<>(studentSubjectService
                .getSubjectsByStudent(studentId));
        return !subjects.isEmpty()
                ? new ResponseEntity<>(subjects, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{subjectId}/students")
    public ResponseEntity<Set<Student>> getStudentsBySubject(@PathVariable Long subjectId) {
        try {
            Set<Student> students = studentSubjectService.getStudentsBySubject(subjectId);
            return !students.isEmpty()
                    ? new ResponseEntity<>(students, HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/student/{studentId}/with-subjects")
    public ResponseEntity<Student> getStudentWithSubjects(@PathVariable Long studentId) {
        try {
            Student student = studentSubjectService.findStudentWithSubjects(studentId);
            return new ResponseEntity<>(student, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/subject/{subjectId}/with-students")
    public ResponseEntity<Subject> getSubjectWithStudents(@PathVariable Long subjectId) {
        try {
            Subject subject = studentSubjectService.findSubjectWithStudents(subjectId);
            return new ResponseEntity<>(subject, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
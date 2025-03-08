package idespring.lab3.controller.subjectcontroller;

import idespring.lab3.model.Subject;
import idespring.lab3.service.subjectservice.SubjectService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subjects")
public class SubjectController {
    private final SubjectService subjectService;

    @Autowired
    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping
    public ResponseEntity<Subject> createSubject(@Valid @RequestBody Subject subject) {
        if (subjectService.existsByName(subject.getName())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(subjectService.addSubject(subject), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Set<Subject>> getSubjects(
            @RequestParam(required = false) String namePattern,
            @RequestParam(required = false) String sort) {
        Set<Subject> subjects = new HashSet<>(subjectService.readSubjects(namePattern, sort));
        return !subjects.isEmpty()
                ? new ResponseEntity<>(subjects, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{subjectId}")
    public ResponseEntity<Subject> getSubjectById(@Positive @NotNull @PathVariable Long subjectId) {
        try {
            Subject subject = subjectService.findById(subjectId);
            return new ResponseEntity<>(subject, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Subject> getSubjectByName(@NotEmpty @PathVariable String name) {
        try {
            Subject subject = subjectService.findByName(name);
            return new ResponseEntity<>(subject, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{subjectId}")
    public ResponseEntity<Void> deleteSubject(@Positive @NotNull @PathVariable Long subjectId) {
        try {
            subjectService.deleteSubject(subjectId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/name/{name}")
    public ResponseEntity<Void> deleteSubjectByName(@NotEmpty @PathVariable String name) {
        try {
            subjectService.deleteSubjectByName(name);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
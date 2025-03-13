package idespring.lab3.controller.markcontroller;

import idespring.lab3.exceptions.SubjectNotAssignedException;
import idespring.lab3.model.Mark;
import idespring.lab3.service.markservice.MarkService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/marks")
public class MarkController {
    private final MarkService markService;

    @Autowired
    public MarkController(MarkService markService) {
        this.markService = markService;
    }

    @PostMapping
    public ResponseEntity<?> createMark(@Valid @RequestBody Mark mark) {
        try {
            return new ResponseEntity<>(markService.addMark(mark), HttpStatus.CREATED);
        } catch (SubjectNotAssignedException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Set<Mark>> getMarks(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long subjectId) {
        Set<Mark> marks = new HashSet<>(markService.readMarks(studentId, subjectId));
        return !marks.isEmpty()
                ? new ResponseEntity<>(marks, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/value/{value}")
    public ResponseEntity<Set<Mark>> getMarksByValue(@Positive @PathVariable int value) {
        Set<Mark> marks = new HashSet<>(markService.findByValue(value));
        return !marks.isEmpty()
                ? new ResponseEntity<>(marks, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/average/student/{studentId}")
    public ResponseEntity<Double> getAverageMarkByStudent(
            @Positive @NotNull @PathVariable Long studentId) {
        Double average = markService.getAverageMarkByStudentId(studentId);
        return average != null
                ? new ResponseEntity<>(average, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/average/subject/{subjectId}")
    public ResponseEntity<Double> getAverageMarkBySubject(
            @Positive @NotNull @PathVariable Long subjectId) {
        Double average = markService.getAverageMarkBySubjectId(subjectId);
        return average != null
                ? new ResponseEntity<>(average, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/delete-specific")
    public ResponseEntity<String> deleteSpecificMark(
            @RequestParam Long studentId,
            @RequestParam String subjectName,
            @RequestParam int markValue,
            @RequestParam(required = false) Long id) {
        try {
            markService.deleteMarkSpecific(studentId, subjectName, markValue, id);
            return ResponseEntity.ok("Specific mark deleted successfully.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the mark.");
        }
    }

    @DeleteMapping("/{markId}")
    public ResponseEntity<Void> deleteMark(@Positive @NotNull @PathVariable Long markId) {
        try {
            markService.deleteMark(markId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
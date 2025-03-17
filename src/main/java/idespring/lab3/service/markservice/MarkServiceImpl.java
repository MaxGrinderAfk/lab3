package idespring.lab3.service.markservice;

import idespring.lab3.config.CacheConfig;
import idespring.lab3.exceptions.SubjectNotAssignedException;
import idespring.lab3.model.Mark;
import idespring.lab3.model.Student;
import idespring.lab3.model.Subject;
import idespring.lab3.repository.markrepo.MarkRepository;
import idespring.lab3.repository.studentrepo.StudentRepository;
import idespring.lab3.repository.subjectrepo.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarkServiceImpl implements MarkService {
    private final MarkRepository markRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final CacheConfig<String, Object> cache;
    private static final Logger logger = LoggerFactory.getLogger(MarkServiceImpl.class);

    @Autowired
    public MarkServiceImpl(MarkRepository markRepository,
                           StudentRepository studentRepository,
                           SubjectRepository subjectRepository,
                           CacheConfig<String, Object> cache) {
        this.markRepository = markRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.cache = cache;
    }

    @Override
    public List<Mark> readMarks(Long studentId, Long subjectId) {
        String cacheKey = "marks-" + (studentId != null ? studentId : "all")
                + "-" + (subjectId != null ? subjectId : "all");
        List<Mark> cachedMarks = (List<Mark>) cache.get(cacheKey);
        if (cachedMarks != null) {
            return cachedMarks;
        }

        logger.info("Fetching marks for student: {}, subject: {}", studentId, subjectId);
        List<Mark> marks;
        if (studentId != null && subjectId != null) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() ->
                            new EntityNotFoundException("Student not found with id: " + studentId));
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() ->
                            new EntityNotFoundException("Subject not found with id: " + subjectId));
            marks = markRepository.findByStudentAndSubject(student, subject);
        } else if (studentId != null) {
            marks = markRepository.findByStudentId(studentId);
        } else if (subjectId != null) {
            marks = markRepository.findBySubjectId(subjectId);
        } else {
            marks = markRepository.findAll();
        }

        cache.put(cacheKey, marks);
        return marks;
    }

    @Override
    public List<Mark> findByValue(int value) {
        String cacheKey = "value-" + value;
        List<Mark> cachedMarks = (List<Mark>) cache.get(cacheKey);
        if (cachedMarks != null) {
            return cachedMarks;
        }

        List<Mark> marks = markRepository.findByValue(value);
        cache.put(cacheKey, marks);
        return marks;
    }

    @Override
    public Double getAverageMarkByStudentId(Long studentId) {
        String cacheKey = "avg-student-" + studentId;
        Double cachedAvg = (Double) cache.get(cacheKey);
        if (cachedAvg != null) {
            return cachedAvg;
        }

        Double avgMark = markRepository.getAverageMarkByStudentId(studentId);
        cache.put(cacheKey, avgMark);
        return avgMark;
    }

    @Override
    public Double getAverageMarkBySubjectId(Long subjectId) {
        String cacheKey = "avg-subject-" + subjectId;
        Double cachedAvg = (Double) cache.get(cacheKey);
        if (cachedAvg != null) {
            return cachedAvg;
        }

        Double avgMark = markRepository.getAverageMarkBySubjectId(subjectId);
        cache.put(cacheKey, avgMark);
        return avgMark;
    }

    @Override
    @Transactional
    public void deleteMarkSpecific(Long studentId, String subjectName, int markValue, Long id) {
        logger.info("Deleting specific mark for student: {}, subject: {}, value: {}, id: {}",
                studentId, subjectName, markValue, id);
        int deletedCount = markRepository.deleteMarkByStudentIdSubjectNameValueAndOptionalId(
                studentId, subjectName, markValue, id);
        if (deletedCount == 0) {
            throw new EntityNotFoundException("Mark not found with the given criteria.");
        }
        cache.remove("marks-" + studentId + "-" + subjectName);
        cache.remove("avg-student-" + studentId);
        cache.remove("avg-subject-" + subjectName);
    }

    @Override
    @Transactional
    public Mark addMark(Mark mark) {
        logger.info("Adding mark for student: {}, subject: {}, value: {}",
                mark.getStudent().getId(), mark.getSubject().getId(), mark.getValue());

        Student student = studentRepository.findById(mark.getStudent().getId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: "
                        + mark.getStudent().getId()));
        Subject subject = subjectRepository.findById(mark.getSubject().getId())
                .orElseThrow(() -> new EntityNotFoundException("Subject not found with id: "
                        + mark.getSubject().getId()));

        boolean hasSubject = student.getSubjects().stream().anyMatch(s ->
                s.getId().equals(subject.getId()));
        if (!hasSubject) {
            throw new SubjectNotAssignedException("Student with ID " + student.getId()
                    + " does not have subject with ID " + subject.getId());
        }

        final Mark savedMark = markRepository.save(mark);
        cache.remove("marks-" + student.getId() + "-" + subject.getId());
        cache.remove("avg-student-" + student.getId());
        cache.remove("avg-subject-" + subject.getId());
        return savedMark;
    }

    @Override
    @Transactional
    public void deleteMark(Long id) {
        logger.info("Deleting mark with id: {}", id);
        markRepository.deleteById(id);
        cache.remove("marks-" + id);
        cache.remove("avg-student-*");
        cache.remove("avg-subject-*");
    }
}
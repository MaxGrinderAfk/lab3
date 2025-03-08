package idespring.lab3.service.markservice;

import idespring.lab3.model.Mark;
import idespring.lab3.model.Student;
import idespring.lab3.model.Subject;
import idespring.lab3.repository.markrepo.MarkRepository;
import idespring.lab3.repository.studentrepo.StudentRepository;
import idespring.lab3.repository.subjectrepo.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarkServiceImpl implements MarkService {
    private final MarkRepository markRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    @Autowired
    public MarkServiceImpl(MarkRepository markRepository,
                           StudentRepository studentRepository,
                           SubjectRepository subjectRepository) {
        this.markRepository = markRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    @Cacheable(value = "marks", key = "'marks-' + (#studentId != null ? #studentId : 'all') + '-' "
            + "+ (#subjectId != null ? #subjectId : 'all')",
            unless = "#result == null or #result.isEmpty()")
    public List<Mark> readMarks(Long studentId, Long subjectId) {
        if (studentId != null && subjectId != null) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new
                            EntityNotFoundException("Student not found with id: " + studentId));
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new
                            EntityNotFoundException("Subject not found with id: " + subjectId));

            return markRepository.findByStudentAndSubject(student, subject);
        } else if (studentId != null) {
            return markRepository.findByStudentId(studentId);
        } else if (subjectId != null) {
            return markRepository.findBySubjectId(subjectId);
        }
        return markRepository.findAll();
    }

    @Override
    @CacheEvict(value = "marks", allEntries = true)
    public List<Mark> findByValue(int value) {
        return markRepository.findByValue(value);
    }

    @Override
    @Cacheable(value = "marks", key = "'avg-student-' + #studentId", unless = "#result == null")
    public Double getAverageMarkByStudentId(Long studentId) {
        return markRepository.getAverageMarkByStudentId(studentId);
    }

    @Override
    @CacheEvict(value = "marks", allEntries = true)
    @Transactional
    public void deleteMarkSpecific(Long studentId, String subjectName, int markValue, Long id) {
        int deletedCount = markRepository
                .deleteMarkByStudentIdSubjectNameValueAndOptionalId(
                        studentId, subjectName, markValue, id);
        if (deletedCount == 0) {
            throw new EntityNotFoundException("Mark not found with the given criteria.");
        }
    }

    @Override
    @Cacheable(value = "marks", key = "'avg-subject-' + #subjectId", unless = "#result == null")
    public Double getAverageMarkBySubjectId(Long subjectId) {
        return markRepository.getAverageMarkBySubjectId(subjectId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "marks", key = "'avg-student-' + #mark.student.id"),
            @CacheEvict(value = "marks", key = "'avg-subject-' + #mark.subject.id"),
            @CacheEvict(value = "marks", allEntries = true)
    })
    @Transactional
    public Mark addMark(Mark mark) {
        return markRepository.save(mark);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "marks", key = "#id"),
            @CacheEvict(value = "marks", allEntries = true)
    })
    public void deleteMark(Long id) {
        markRepository.deleteById(id);
    }
}
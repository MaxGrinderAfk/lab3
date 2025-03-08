package idespring.lab3.service.studentsubjserv;

import idespring.lab3.model.Student;
import idespring.lab3.model.Subject;
import idespring.lab3.repository.studentrepo.StudentRepository;
import idespring.lab3.repository.subjectrepo.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class StudentSubjectServiceImpl implements StudentSubjectService {
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    private static final String STUDENTERR = "Student not found";
    private static final String SUBJECTERR = "Subject not found";

    @Autowired
    public StudentSubjectServiceImpl(StudentRepository studentRepository,
                                     SubjectRepository subjectRepository) {
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    @CacheEvict(value = {"studentSubjects", "students"}, allEntries = true)
    @Transactional
    public void addSubjectToStudent(Long studentId, Long subjectId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException(STUDENTERR));

        subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException(SUBJECTERR));

        studentRepository.addSubject(studentId, subjectId);
    }

    @Override
    @CacheEvict(value = {"studentSubjects", "students"}, allEntries = true)
    @Transactional
    public void removeSubjectFromStudent(Long studentId, Long subjectId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException(STUDENTERR));

        subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException(SUBJECTERR));

        studentRepository.removeSubject(studentId, subjectId);
    }

    @Override
    @Cacheable(value = "studentSubjects", key = "'subjects-' + #studentId",
            unless = "#result == null or #result.isEmpty()")
    public List<Subject> getSubjectsByStudent(Long studentId) {
        return subjectRepository.findByStudentId(studentId);
    }

    @Override
    @Cacheable(value = "studentSubjects", key = "'students-' + #subjectId",
            unless = "#result == null or #result.isEmpty()")
    public Set<Student> getStudentsBySubject(Long subjectId) {
        Subject subject = subjectRepository.findByIdWithStudents(subjectId)
                .orElseThrow(() -> new EntityNotFoundException(SUBJECTERR));

        return subject.getStudents();
    }

    @Override
    @Cacheable(value = "studentSubjects", key = "'student-with-subjects-' + #studentId",
            unless = "#result == null or #result.getSubjects().isEmpty()")
    public Student findStudentWithSubjects(Long studentId) {
        return studentRepository.findByIdWithSubjects(studentId)
                .orElseThrow(() -> new EntityNotFoundException(STUDENTERR));
    }

    @Override
    @Cacheable(value = "studentSubjects", key = "'subject-with-students-' + #subjectId",
            unless = "#result == null or #result.getStudents().isEmpty()")
    public Subject findSubjectWithStudents(Long subjectId) {
        return subjectRepository.findByIdWithStudents(subjectId)
                .orElseThrow(() -> new EntityNotFoundException(SUBJECTERR));
    }
}
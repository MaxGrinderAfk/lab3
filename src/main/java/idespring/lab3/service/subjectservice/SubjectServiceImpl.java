package idespring.lab3.service.subjectservice;

import idespring.lab3.config.CacheConfig;
import idespring.lab3.model.Subject;
import idespring.lab3.repository.subjectrepo.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubjectServiceImpl implements SubjectService {
    private final SubjectRepository subjectRepository;
    private final CacheConfig<String, Object> cache;
    private static final String NOTFOUND = "Subject not found with id: ";
    private static final Logger logger = LoggerFactory.getLogger(SubjectServiceImpl.class);

    @Autowired
    public SubjectServiceImpl(SubjectRepository subjectRepository,
                              CacheConfig<String, Object> cache) {
        this.subjectRepository = subjectRepository;
        this.cache = cache;
    }

    @Override
    public List<Subject> readSubjects(String namePattern, String sort) {
        String cacheKey = namePattern + "-" + (sort != null ? sort : "default");
        if (cache.get(cacheKey) != null) {
            return (List<Subject>) cache.get(cacheKey);
        }

        final long start = System.nanoTime();
        logger.info("Fetching subjects from database for namePattern: {}, sort: {}",
                namePattern, sort);

        List<Subject> subjects;
        if (namePattern != null) {
            subjects = subjectRepository.findByNameContaining(namePattern);
        } else if ("asc".equalsIgnoreCase(sort)) {
            subjects = subjectRepository.findAllByOrderByNameAsc();
        } else {
            subjects = subjectRepository.findAll();
        }

        cache.put(cacheKey, subjects);
        long end = System.nanoTime();
        logger.info("Execution time for readSubjects: {} ms", (end - start) / 1_000_000);
        return subjects;
    }

    @Override
    public Subject findById(Long id) {
        String cacheKey = "subject-" + id;
        if (cache.get(cacheKey) != null) {
            return (Subject) cache.get(cacheKey);
        }

        long start = System.nanoTime();
        logger.info("Fetching subject from database for id: {}", id);

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOTFOUND + id));

        cache.put(cacheKey, subject);
        long end = System.nanoTime();
        logger.info("Execution time for findById: {} ms", (end - start) / 1_000_000);
        return subject;
    }

    @Override
    public Subject findByName(String name) {
        String cacheKey = "subject-" + name;
        if (cache.get(cacheKey) != null) {
            return (Subject) cache.get(cacheKey);
        }

        long start = System.nanoTime();
        logger.info("Fetching subject from database for name: {}", name);

        Subject subject = subjectRepository.findByName(name)
                .orElseThrow(() -> new
                        EntityNotFoundException("Subject not found with name: " + name));

        cache.put(cacheKey, subject);
        long end = System.nanoTime();
        logger.info("Execution time for findByName: {} ms", (end - start) / 1_000_000);
        return subject;
    }

    @Override
    public Subject addSubject(Subject subject) {
        final long start = System.nanoTime();
        logger.info("Saving subject: {}", subject.getName());

        Subject savedSubject = subjectRepository.save(subject);
        cache.put("subject-" + savedSubject.getId(), savedSubject);
        cache.put("subject-" + savedSubject.getName(), savedSubject);

        long end = System.nanoTime();
        logger.info("Execution time for addSubject: {} ms", (end - start) / 1_000_000);
        return savedSubject;
    }

    @Override
    @Transactional
    public void deleteSubject(Long id) {
        logger.info("Deleting subject with id: {}", id);
        if (!subjectRepository.existsById(id)) {
            throw new EntityNotFoundException(NOTFOUND + id);
        }
        subjectRepository.deleteById(id);
        cache.remove("subject-" + id);
        logger.info("Subject with id {} deleted", id);
    }

    @Override
    @Transactional
    public void deleteSubjectByName(String name) {
        logger.info("Deleting subject with name: {}", name);
        if (!subjectRepository.existsByName(name)) {
            throw new EntityNotFoundException("Subject not found with name: " + name);
        }
        subjectRepository.deleteByName(name);
        cache.remove("subject-" + name);
        logger.info("Subject with name {} deleted", name);
    }

    @Override
    public boolean existsByName(String name) {
        logger.info("Checking existence of subject with name: {}", name);
        return subjectRepository.existsByName(name);
    }
}


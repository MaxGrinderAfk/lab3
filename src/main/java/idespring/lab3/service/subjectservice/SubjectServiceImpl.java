package idespring.lab3.service.subjectservice;

import idespring.lab3.model.Subject;
import idespring.lab3.repository.subjectrepo.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubjectServiceImpl implements SubjectService {
    private final SubjectRepository subjectRepository;
    private static final String NOTFOUND = "Subject not found with id: ";

    @Autowired
    public SubjectServiceImpl(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Override
    @Cacheable(value = "subjects",
            key = "#namePattern + '-' + (#sort != null ? #sort : 'default')",
            unless = "#result == null or #result.isEmpty()")
    public List<Subject> readSubjects(String namePattern, String sort) {
        if (namePattern != null) {
            return subjectRepository.findByNameContaining(namePattern);
        }
        if ("asc".equalsIgnoreCase(sort)) {
            return subjectRepository.findAllByOrderByNameAsc();
        }
        return subjectRepository.findAll();
    }

    @Override
    @Cacheable(value = "subjects", key = "#id", unless = "#result == null")
    public Subject findById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOTFOUND + id));
    }

    @Override
    @Cacheable(value = "subjects", key = "#name")
    public Subject findByName(String name) {
        return subjectRepository.findByName(name)
                .orElseThrow(() -> new
                        EntityNotFoundException("Subject not found with name: " + name));
    }


    @Override
    @Caching(
            put = {
                    @CachePut(value = "subjects", key = "#result.id"),
                    @CachePut(value = "subjects", key = "#result.name")
            },
            evict = {
                    @CacheEvict(value = "subjects", key = "'exists-' + #result.name")
            }
    )
    public Subject addSubject(Subject subject) {
        return subjectRepository.save(subject);
    }


    @Override
    @CacheEvict(value = "subjects", key = "#id")
    @Transactional
    public void deleteSubject(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new EntityNotFoundException(NOTFOUND + id);
        }
        subjectRepository.deleteById(id);
    }


    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "subjects", key = "#name"),
                    @CacheEvict(value = "subjects", key = "'exists-' + #name")
            }
    )
    @Transactional
    public void deleteSubjectByName(String name) {
        if (!subjectRepository.existsByName(name)) {
            throw new EntityNotFoundException("Subject not found with name: " + name);
        }
        subjectRepository.deleteByName(name);
    }

    @Override
    @Cacheable(value = "subjects", key = "'exists-' + #name")
    public boolean existsByName(String name) {
        return subjectRepository.existsByName(name);
    }
}
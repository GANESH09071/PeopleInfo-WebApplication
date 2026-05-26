package com.peopleinfo.service;

import com.peopleinfo.model.HiringRequirement;
import com.peopleinfo.repository.HiringRequirementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HiringService {

    private final HiringRequirementRepository hiringRepository;

    public HiringRequirement save(HiringRequirement hiring) {
        return hiringRepository.save(hiring);
    }

    public List<HiringRequirement> getAll() {
        return hiringRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<HiringRequirement> getOpenJobs() {
        return hiringRepository.findByStatus(HiringRequirement.HiringStatus.OPEN);
    }

    public Optional<HiringRequirement> findById(Long id) {
        return hiringRepository.findById(id);
    }

    public void delete(Long id) {
        hiringRepository.deleteById(id);
    }

    public long countOpenJobs() {
        return hiringRepository.countByStatus(HiringRequirement.HiringStatus.OPEN);
    }

    public HiringRequirement updateStatus(Long id, HiringRequirement.HiringStatus status) {
        HiringRequirement hr = hiringRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hiring requirement not found"));
        hr.setStatus(status);
        return hiringRepository.save(hr);
    }
}

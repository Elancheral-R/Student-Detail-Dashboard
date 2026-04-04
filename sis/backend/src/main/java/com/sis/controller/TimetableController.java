package com.sis.controller;

import com.sis.exception.GlobalExceptionHandler.BusinessException;
import com.sis.model.Timetable;
import com.sis.repository.TimetableRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Timetable management.
 * Includes server-side conflict detection before saving.
 */
@RestController
@RequestMapping("/timetable")
public class TimetableController {

    private final TimetableRepository timetableRepo;

    public TimetableController(TimetableRepository timetableRepo) {
        this.timetableRepo = timetableRepo;
    }

    @GetMapping
    public ResponseEntity<List<Timetable>> getAll(
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) String year) {
        if (semester != null && year != null)
            return ResponseEntity.ok(timetableRepo.findBySemesterAndAcademicYear(semester, year));
        return ResponseEntity.ok(timetableRepo.findAll());
    }

    @PostMapping
    public ResponseEntity<Timetable> create(@RequestBody Timetable entry) {
        // Conflict detection: same room, same day, overlapping times
        List<Timetable> conflicts = timetableRepo.findConflicts(
            entry.getRoomNo(), entry.getDayOfWeek(),
            entry.getStartTime(), entry.getEndTime(), null
        );
        if (!conflicts.isEmpty())
            throw new BusinessException("Schedule conflict detected for room " + entry.getRoomNo() +
                " on " + entry.getDayOfWeek() + " between " + entry.getStartTime() + "–" + entry.getEndTime());

        return ResponseEntity.status(201).body(timetableRepo.save(entry));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timetableRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

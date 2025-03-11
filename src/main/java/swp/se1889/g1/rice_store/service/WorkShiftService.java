package swp.se1889.g1.rice_store.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swp.se1889.g1.rice_store.entity.Shift;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.entity.WorkShift;
import swp.se1889.g1.rice_store.repository.EmployeeRepository;
import swp.se1889.g1.rice_store.repository.ShiftRepository;
import swp.se1889.g1.rice_store.repository.UserRepository;
import swp.se1889.g1.rice_store.repository.WorkShiftRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkShiftService {

    private final WorkShiftRepository workShiftRepository;
    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;

    @Autowired
    public WorkShiftService(WorkShiftRepository workShiftRepository,
                            ShiftRepository shiftRepository,
                            UserRepository userRepository) {
        this.workShiftRepository = workShiftRepository;
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
    }

    public List<WorkShift> getAllWorkShifts() {
        return workShiftRepository.findAll();
    }

    public List<WorkShift> getWorkShiftsByDateRange(LocalDate startDate, LocalDate endDate) {
        return workShiftRepository.findByWorkDateBetween(startDate, endDate);
    }

    public List<WorkShift> getWorkShiftsByDate(LocalDate date) {
        return workShiftRepository.findByWorkDate(date);
    }

    public List<WorkShift> getWorkShiftsByEmployee(Long employeeId) {
        return workShiftRepository.findByEmployee(employeeId);
    }

    public WorkShift getWorkShiftById(Long id) {
        return workShiftRepository.findById(id).orElse(null);
    }

    @Transactional
    public void assignEmployeeToShift(Long employeeId, Long shiftId, LocalDate workDate) {
        // Check if employee and shift exist
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));

        // Check if assignment already exists
        WorkShift existingAssignment = workShiftRepository
                .findByEmployeeAndShiftAndWorkDate(employeeId, shiftId, workDate);

        if (existingAssignment != null) {
            throw new RuntimeException("Employee already assigned to this shift on this date");
        }

        // Create new work shift
        WorkShift workShift = new WorkShift();
        workShift.setEmployee(employeeId);
        workShift.setShift(shiftId);
        workShift.setWorkDate(workDate);

        // Set scheduled times
        LocalDateTime scheduledStart = LocalDateTime.of(workDate, shift.getStartTime());
        LocalDateTime scheduledEnd = LocalDateTime.of(workDate, shift.getEndTime());

        // Handle shifts that span midnight
        if (shift.getEndTime().isBefore(shift.getStartTime())) {
            scheduledEnd = scheduledEnd.plusDays(1);
        }

        workShift.setScheduledStartTime(scheduledStart);
        workShift.setScheduledEndTime(scheduledEnd);

        workShift.setCreatedAt(LocalDateTime.now());
        workShift.setUpdatedAt(LocalDateTime.now());
        workShiftRepository.save(workShift);
    }

    @Transactional
    public void removeEmployeeFromShift(Long employeeId, Long shiftId, LocalDate workDate) {
        WorkShift workShift = workShiftRepository
                .findByEmployeeAndShiftAndWorkDate(employeeId, shiftId, workDate);

        if (workShift != null) {
            workShiftRepository.delete(workShift);
        } else {
            throw new RuntimeException("Assignment not found");
        }
    }

    @Transactional
    public void updateEmployeeShift(Long workShiftId, Long newEmployeeId) {
        WorkShift workShift = workShiftRepository.findById(workShiftId)
                .orElseThrow(() -> new RuntimeException("Work shift not found"));

        User newEmployee = userRepository.findById(newEmployeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        workShift.setEmployee(newEmployeeId);
        workShiftRepository.save(workShift);
    }
}

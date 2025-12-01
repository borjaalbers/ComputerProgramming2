package com.gymflow.service;

import com.gymflow.dao.WorkoutCompletionDao;
import com.gymflow.dao.WorkoutCompletionDaoImpl;
import com.gymflow.model.WorkoutCompletion;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of WorkoutCompletionService.
 */
public class WorkoutCompletionServiceImpl implements WorkoutCompletionService {
    private final WorkoutCompletionDao completionDao;

    public WorkoutCompletionServiceImpl() {
        this.completionDao = new WorkoutCompletionDaoImpl();
    }

    @Override
    public Optional<WorkoutCompletion> markCompleted(long workoutPlanId, long memberId, Long classSessionId, String notes) {
        if (workoutPlanId <= 0 || memberId <= 0) {
            System.err.println("Invalid workout plan or member ID");
            return Optional.empty();
        }

        return completionDao.markCompleted(workoutPlanId, memberId, classSessionId, notes);
    }

    @Override
    public boolean isCompleted(long workoutPlanId, long memberId) {
        if (workoutPlanId <= 0 || memberId <= 0) {
            return false;
        }

        return completionDao.isCompleted(workoutPlanId, memberId);
    }

    @Override
    public List<WorkoutCompletion> getCompletionsByMember(long memberId) {
        if (memberId <= 0) {
            return List.of();
        }

        return completionDao.findByMemberId(memberId);
    }

    @Override
    public boolean unmarkCompleted(long workoutPlanId, long memberId) {
        if (workoutPlanId <= 0 || memberId <= 0) {
            return false;
        }

        return completionDao.delete(workoutPlanId, memberId);
    }
}


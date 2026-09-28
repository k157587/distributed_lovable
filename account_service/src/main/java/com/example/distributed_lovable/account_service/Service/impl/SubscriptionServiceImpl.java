package com.example.distributed_lovable.account_service.Service.impl;

import com.example.distributed_lovable.account_service.Service.SubscriptionService;
import com.example.distributed_lovable.account_service.dto.subscription.SubscriptionResponse;
import com.example.distributed_lovable.account_service.entity.Plan;
import com.example.distributed_lovable.account_service.entity.Subscription;
import com.example.distributed_lovable.account_service.entity.User;
import com.example.distributed_lovable.account_service.mapper.SubscriptionMapper;
import com.example.distributed_lovable.account_service.repository.PlanRepository;
import com.example.distributed_lovable.account_service.repository.SubscriptionRepository;
import com.example.distributed_lovable.account_service.repository.UserRepository;
import com.example.distributed_lovable.common_lib.dto.PlanDto;
import com.example.distributed_lovable.common_lib.enums.SubscriptionStatus;
import com.example.distributed_lovable.common_lib.error.ResourceNotFoundException;
import com.example.distributed_lovable.common_lib.security.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Slf4j

@RequiredArgsConstructor
@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final AuthUtil authUtil;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final Integer FREE_TIER_PROJECTS_ALLOWED = 100;

    @Override
    public SubscriptionResponse getCurrentSubscription() {
        Long userId = authUtil.getCurrentUserId();

        var currentSubscription =  subscriptionRepository.findByUserIdAndStatusIn(userId,
                Set.of(
                       SubscriptionStatus.ACTIVE,SubscriptionStatus.TRIALING,SubscriptionStatus.PAST_DUE
                )).orElse(
                        new Subscription()
        );


        return subscriptionMapper.toSubscriptionResponse(currentSubscription);
    }



    @Override
    public void activateSubscription(Long userId, Long planId, String subscriptionId, String customerId) {

        boolean exists = subscriptionRepository.existsByStripeSubscriptionId(subscriptionId);

        if (exists) return;

        User user = getUser(userId);
        Plan plan = getPlan(planId);

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .stripeSubscriptionId(subscriptionId)
                .status(SubscriptionStatus.INCOMPLETE)
                .build();

        subscriptionRepository.save(subscription);

    }

    @Override
    @Transactional
    public void updateSubscription(String subscriptionId, SubscriptionStatus status, Instant periodStart, Instant periodEnd, Boolean cancelAtPeriodEnd, Long planId) {
        Subscription subscription = getSubscription(subscriptionId);

        boolean hasSubscriptionUpdated = false;

        if (status != null && status != subscription.getStatus()){
            subscription.setStatus(status);
            hasSubscriptionUpdated = true;
        }

        if (periodStart != null && !periodStart.equals(subscription.getCurrentPeriodStart())){
            subscription.setCurrentPeriodStart(periodStart);
            hasSubscriptionUpdated = true;
        }


        if (periodEnd != null && !periodEnd.equals(subscription.getCurrentPeriodEnd())){
            subscription.setCurrentPeriodEnd(periodEnd);
            hasSubscriptionUpdated = true;
        }


        if (cancelAtPeriodEnd != null && !cancelAtPeriodEnd.equals(subscription.getCancelAtPeriodEnd())){
            subscription.setCancelAtPeriodEnd(cancelAtPeriodEnd);
            hasSubscriptionUpdated = true;
        }


        if (planId != null && !planId.equals(subscription.getPlan().getId())){
            Plan newplan = getPlan(planId);

            subscription.setPlan(newplan);
            hasSubscriptionUpdated = true;
        }

        if (hasSubscriptionUpdated){
            log.debug("subscription has been updated : {}",subscriptionId);
            subscriptionRepository.save(subscription);
        }

    }

    @Override
    public void cancelSubscription(String subscriptionId) {
        Subscription subscription = getSubscription(subscriptionId);

        subscription.setStatus(SubscriptionStatus.CANCELLED);

        subscriptionRepository.save(subscription);



    }

    @Override
    public void renewSubscriptionPeriod(String subId, Instant periodStart, Instant periodEnd) {
        Subscription subscription = getSubscription(subId);

        Instant newStart = periodStart != null ? periodStart : subscription.getCurrentPeriodEnd();

        subscription.setCurrentPeriodStart(newStart);
        subscription.setCurrentPeriodEnd(periodEnd);

        if (subscription.getStatus() == SubscriptionStatus.PAST_DUE || subscription.getStatus() == SubscriptionStatus.INCOMPLETE){
            subscription.setStatus(SubscriptionStatus.ACTIVE);
        }

        subscriptionRepository.save(subscription);
    }


    @Override
    public void markSubscriptionPastDue(String subId) {
        Subscription subscription = getSubscription(subId);

        if (subscription.getStatus() == SubscriptionStatus.PAST_DUE){
            log.debug("subscription is already past due,gatewaySubscriptionId : {}",subId);
            return;
        }

        subscription.setStatus(SubscriptionStatus.PAST_DUE);

        subscriptionRepository.save(subscription);


    }

    @Override
    public PlanDto getCurrentSubscriptionPlanByUser() {
        SubscriptionResponse currentSubscription = getCurrentSubscription();

        return currentSubscription.plan();
    }


    //utility methods

    private User getUser(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user",userId.toString()));
    }

    private Plan getPlan(Long planId){
        return planRepository.findById(planId).orElseThrow(
                () -> new ResourceNotFoundException("Plan",planId.toString())
        );

    }


    private Subscription getSubscription(String subId) {
        return subscriptionRepository.findByStripeSubscriptionId(subId).orElseThrow(() ->
                new ResourceNotFoundException("subscription", subId));
    }
}

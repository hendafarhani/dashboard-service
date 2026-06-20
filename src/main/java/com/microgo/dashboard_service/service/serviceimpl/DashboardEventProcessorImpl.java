package com.microgo.dashboard_service.service.serviceimpl;

import com.microgo.dashboard_service.mapper.RideDashboardMessageMapper;
import com.microgo.dashboard_service.domain.DashboardProjection;
import com.microgo.dashboard_service.domain.ResolvedDashboardEvent;
import com.microgo.dashboard_service.domain.RideDashboardMessage;
import com.microgo.dashboard_service.kafka.publisher.DashboardAckPublisher;
import com.microgo.dashboard_service.service.DashboardEventProcessor;
import com.microgo.dashboard_service.service.DashboardStreamingService;
import com.microgo.dashboard_service.service.EventProjectionRouter;
import com.microgo.dashboard_service.service.OutboxEventResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardEventProcessorImpl implements DashboardEventProcessor {

    private final OutboxEventResolver outboxEventResolver;
    private final EventProjectionRouter eventProjectionRouter;
    private final RideDashboardMessageMapper rideDashboardMessageMapper;
    private final DashboardStreamingService dashboardStreamingService;
    private final DashboardAckPublisher dashboardAckPublisher;

    @Override
    public boolean process(String message) {
        try {
            ResolvedDashboardEvent resolvedEvent = outboxEventResolver.resolve(message).orElse(null);
            if (eventWasNotResolved(resolvedEvent)) {
                return false;
            }

            DashboardProjection projection = eventProjectionRouter.route(
                    resolvedEvent.eventType(),
                    resolvedEvent.outboxEvent()
            );
            RideDashboardMessage dashboardMessage = rideDashboardMessageMapper.map(resolvedEvent, projection);

            dashboardStreamingService.streamRideEvent(dashboardMessage);
            dashboardAckPublisher.publishWebsocketPublished(resolvedEvent.outboxEvent().getId());
            return true;
        } catch (Exception ex) {
            log.warn("Unable to process dashboard event {}", message, ex);
            return false;
        }
    }

    private boolean eventWasNotResolved(ResolvedDashboardEvent resolvedEvent) {
        return resolvedEvent == null;
    }
}

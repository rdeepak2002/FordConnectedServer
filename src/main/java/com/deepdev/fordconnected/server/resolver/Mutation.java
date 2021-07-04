package com.deepdev.fordconnected.server.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetchingEnvironment;
import graphql.servlet.GraphQLContext;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.deepdev.fordconnected.server.model.Visit;
import com.deepdev.fordconnected.server.repository.VisitRepository;

@Component
public class Mutation implements GraphQLMutationResolver {
    private VisitRepository visitRepository;

    @Autowired
    public Mutation(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    public Visit createVisit(String browserId, String deviceId, Boolean isMobile, DataFetchingEnvironment env) {
        LocalDateTime currentTime = LocalDateTime.now();

        GraphQLContext context = env.getContext();
        HttpServletRequest request = context.getHttpServletRequest().get();
        String ip = getClientIp(request);

        String userAgentId = getUserAgentId(request);

        Optional<Visit> previousVisit = visitRepository.findByIpAndBrowserIdAndIsMobileOrDeviceId(ip, browserId,
                isMobile, deviceId);
        Visit visit = previousVisit.isPresent() ? previousVisit.get() : new Visit();

        visit.setIp(ip);
        visit.setBrowserId(userAgentId == null ? browserId : userAgentId);
        visit.setDeviceId(deviceId);
        visit.setIsMobile(isMobile);
        visit.setCreatedAt(visit.getCreatedAt() == null ? currentTime : visit.getCreatedAt());
        visit.setLastVisited(currentTime);
        visit.addDateVisited(currentTime);

        visitRepository.save(visit);

        return visit;
    }

    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";

        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }

        return remoteAddr;
    }

    private String getUserAgentId(HttpServletRequest request) {
        String userAgentId = "";

        if (request != null) {
            userAgentId = request.getHeader("User-Agent");
        }

        return userAgentId;
    }
}
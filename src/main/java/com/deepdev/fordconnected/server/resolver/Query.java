package com.deepdev.fordconnected.server.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.deepdev.fordconnected.server.model.Visit;
import com.deepdev.fordconnected.server.repository.VisitRepository;

@Component
public class Query implements GraphQLQueryResolver {
    private VisitRepository visitRepository;

    @Autowired
    public Query(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    public Iterable<Visit> findAllVisits() {
        return visitRepository.findAll();
    }

    public long countVisits() {
        return visitRepository.count();
    }
}
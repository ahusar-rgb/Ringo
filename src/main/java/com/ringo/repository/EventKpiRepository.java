//package com.ringo.repository;
//
//import com.amazonaws.services.sagemaker.model.QueryFilters;
//import com.ringo.dto.common.ActionDynamics;
//import com.ringo.dto.common.CountByDate;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import jakarta.persistence.Query;
//import org.hibernate.query.NativeQuery;
//import org.hibernate.transform.ResultTransformer;
//import org.springframework.stereotype.Repository;
//
//import java.text.MessageFormat;
//import java.time.LocalDate;
//import java.util.List;
//
//@Repository
//public class EventKpiRepository {
//
//    @PersistenceContext
//    private EntityManager entityManager;
//
//    public ActionDynamics findTicketsSoldBetweenDates(LocalDate startDate, LocalDate endDate, Long id) {
//        String query =
//                """
//                SELECT _date as date,
//                (
//                SELECT count(*) as count
//                FROM ticket
//                WHERE DATE(time_of_submission) = _date
//                AND event_id = {0}
//                )
//                FROM generate_series(DATE({1}), DATE({2}), INTERVAL '1 day') _date;
//                """;
//
//        query = MessageFormat.format(query, id, startDate, endDate);
//        List<CountByDate> data = entityManager.createQuery(query)
//                .unwrap(org.hibernate.query.NativeQuery.class)
//                .setResultTransformer(getCountByDateResultTransformer()).getResultList();
//
//        ActionDynamics actionDynamics = new ActionDynamics();
//        actionDynamics.setAction("Tickets sold");
//        actionDynamics.setData(data);
//        return actionDynamics;
//    }
//
//    private ResultTransformer<CountByDate> getCountByDateResultTransformer() {
//        return (tuple, aliases) -> {
//            CountByDate countByDate = new CountByDate();
//            countByDate.setDate((LocalDate) tuple[0]);
//            countByDate.setCount((Integer) tuple[1]);
//            return countByDate;
//        };
//    }
//}

//package com.ringo.service.company;
//
//import com.ringo.dto.common.ActionDynamics;
//import com.ringo.dto.common.CountByDate;
//import com.ringo.exception.UserException;
//import com.ringo.repository.EventKpiRepository;
//import com.ringo.repository.EventRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class EventKpiService {
//
//    private final EventKpiRepository eventKpiRepository;
//    private final EventRepository eventRepository;
//
//    public ActionDynamics getTicketsSoldByDate(LocalDate startDate, LocalDate endDate, Long eventId) {
//
//        if(startDate == null)
//            startDate = LocalDate.now().minusDays(7);
//        if(endDate == null)
//            endDate = LocalDate.now();
//
//        if(startDate.isAfter(endDate))
//            throw new UserException("startDate must be before endDate");
//
//        if(eventId == null)
//            throw new UserException("eventId must not be null");
//        if(!eventRepository.existsById(eventId))
//            throw new UserException("Event#" + eventId + " was not found");
//
//        return eventKpiRepository.findTicketsSoldBetweenDates(startDate, endDate, eventId);
//    }
//
//    public ActionDynamics getSavesByDate() {
//        return null;
//    }
//
//    public ActionDynamics getViewsByDate() {
//        return null;
//    }
//}

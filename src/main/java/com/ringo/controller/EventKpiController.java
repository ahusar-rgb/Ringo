//package com.ringo.controller;
//
//import com.ringo.dto.common.ActionDynamics;
//import com.ringo.service.company.EventKpiService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/statistics")
//@RequiredArgsConstructor
//public class EventKpiController {
//    private final EventKpiService eventKpiService;
//
//    @GetMapping(value = "events/{id}/tickets-sold", produces = {"application/json"})
//    public ResponseEntity<ActionDynamics> getTicketsSold(
//            @PathVariable("id") Long id,
//            LocalDate startDate,
//            LocalDate endDate
//    ) {
//        return ResponseEntity
//                .ok()
//                .body(eventKpiService.getTicketsSoldByDate(startDate, endDate, id));
//    }
//
//}

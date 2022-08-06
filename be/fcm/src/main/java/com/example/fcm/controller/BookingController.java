package com.example.fcm.controller;

import com.example.fcm.model.entity.Booking;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * com.example.fcm.controller
 * Created by Admin
 * Date 8/4/2022 - 3:25 PM
 * Description: ...
 */

@RestController
@RequestMapping("/booking")
public class BookingController {
    @PostMapping("/")
    public ResponseEntity<?>sendInformationBooking(@RequestBody Booking booking){
        return ResponseEntity.ok(booking);
    }

}

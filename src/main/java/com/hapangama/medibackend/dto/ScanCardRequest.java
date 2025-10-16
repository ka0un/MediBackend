package com.hapangama.medibackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanCardRequest {
    private String cardNumber; // Digital health card number (from barcode/QR code)
    private String staffId; // ID of staff member accessing the records
    private String purpose; // Purpose of accessing medical records
}

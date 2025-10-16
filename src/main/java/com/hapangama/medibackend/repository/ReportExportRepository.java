package com.hapangama.medibackend.repository;

import com.hapangama.medibackend.model.ReportExport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportExportRepository extends JpaRepository<ReportExport, Long> {
}

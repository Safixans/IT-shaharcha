package com.itshaharcha.portfolio.mapper;

import com.itshaharcha.portfolio.dto.response.CertificateResponse;
import com.itshaharcha.portfolio.dto.response.EducationResponse;
import com.itshaharcha.portfolio.dto.response.PortfolioItemResponse;
import com.itshaharcha.portfolio.entity.Certificate;
import com.itshaharcha.portfolio.entity.Education;
import com.itshaharcha.portfolio.entity.PortfolioItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PortfolioMapper {

    CertificateResponse toCertificateResponse(Certificate certificate);

    EducationResponse toEducationResponse(Education education);

    PortfolioItemResponse toItemResponse(PortfolioItem item);
}

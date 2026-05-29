package com.itshaharcha.user.mapper;

import com.itshaharcha.user.dto.response.CertificateResponse;
import com.itshaharcha.user.dto.response.EducationResponse;
import com.itshaharcha.user.dto.response.PortfolioItemResponse;
import com.itshaharcha.user.dto.response.ProfileResponse;
import com.itshaharcha.user.entity.Certificate;
import com.itshaharcha.user.entity.EducationHistory;
import com.itshaharcha.user.entity.PortfolioItem;
import com.itshaharcha.user.entity.Profile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    ProfileResponse toResponse(Profile profile);

    CertificateResponse toResponse(Certificate certificate);

    EducationResponse toResponse(EducationHistory education);

    PortfolioItemResponse toResponse(PortfolioItem item);
}

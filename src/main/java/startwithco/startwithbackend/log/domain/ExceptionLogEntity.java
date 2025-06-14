package startwithco.startwithbackend.log.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import startwithco.startwithbackend.base.BaseTimeEntity;

@Entity
@Table(name = "EXCEPTION_LOG_ENTITY")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public class ExceptionLogEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exception_log_seq")
    private Long exceptionLogSeq;

    @Column(name = "http_status", nullable = false)
    private int httpStatus;

    @Column(name = "error_code", nullable = false)
    private String errorCode;

    @Lob
    @Column(name = "message", nullable = false, columnDefinition = "LONGTEXT")
    private String message;

    @Column(name = "request_uri", nullable = false)
    private String requestUri;

    @Column(name = "request_body", nullable = false, columnDefinition = "LONGTEXT")
    private String requestBody;

    @Lob
    @Column(name = "method_name", nullable = false, columnDefinition = "LONGTEXT")
    private String methodName;
}

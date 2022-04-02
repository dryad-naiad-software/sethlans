package com.dryadandnaiad.sethlans.models.system;

import com.dryadandnaiad.sethlans.models.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Entity
public class Notification extends AbstractModel {
    private Date messageDate;
    private String message;
}

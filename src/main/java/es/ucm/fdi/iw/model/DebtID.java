package es.ucm.fdi.iw.model;

import java.io.Serializable;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * Member ID
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Embeddable
public class DebtID implements Serializable {
    
    private long groupId;
    private long debtorId;
    private long debtOwnerId;

}

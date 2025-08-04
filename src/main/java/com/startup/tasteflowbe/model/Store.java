package com.startup.tasteflowbe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.startup.tasteflowbe.enums.Region;
import com.startup.tasteflowbe.enums.StoreStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "stores")
@Data
@NoArgsConstructor
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "contact_info", length = 100)
    private String contactInfo;

    @Column(name = "business_hours", length = 100)
    private String businessHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "region", nullable = false, columnDefinition = "TEXT")
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private StoreStatus status;

    @OneToOne
    @JoinColumn(name = "manager_id", referencedColumnName = "user_id", unique = true)
    private User manager;

    @Override
    public String toString() {
        return "Store{" +
                "storeId=" + storeId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", contactInfo='" + contactInfo + '\'' +
                ", businessHours='" + businessHours + '\'' +
                ", region=" + region +
                ", status=" + status +
                ", manager=" + (manager != null ? manager.getUserId() : null) +
                '}';
    }

    @Column(name = "province", length = 100)
    private String province;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "village", length = 100)
    private String village;

    @OneToMany(mappedBy = "store")
    @JsonIgnore
    private List<StoreStaff> staffAssignments;

}

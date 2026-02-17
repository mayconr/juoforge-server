package com.github.mayconr.juoserver.standard.npc;

import com.github.mayconr.juoserver.standard.npc.banker.BankerBehaviorProfile;
import com.github.mayconr.juoserver.standard.npc.vendor.VendorBehaviorProfile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StandardConfiguration {

    @Bean
    public VendorBehaviorProfile vendorBehaviorProfile() {
        return new VendorBehaviorProfile();
    }

    @Bean
    public BankerBehaviorProfile bankerBehaviorProfile() {
        return new BankerBehaviorProfile();
    }

    @Bean
    public DialogueReactiveAI dialogueReactiveAI() {
        return new DialogueReactiveAI();
    }
}

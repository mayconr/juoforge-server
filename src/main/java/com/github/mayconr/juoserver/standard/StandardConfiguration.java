package com.github.mayconr.juoserver.standard;

import com.github.mayconr.juoserver.standard.ai.animal.AnimalBehaviorProfile;
import com.github.mayconr.juoserver.standard.ai.animal.PassiveAnimalAI;
import com.github.mayconr.juoserver.standard.ai.npc.DialogueReactiveAI;
import com.github.mayconr.juoserver.standard.ai.npc.banker.BankerBehaviorProfile;
import com.github.mayconr.juoserver.standard.ai.npc.vendor.VendorBehaviorProfile;
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

    public AnimalBehaviorProfile animalProfile() {
        return new AnimalBehaviorProfile();
    }

    @Bean
    public DialogueReactiveAI dialogueReactiveAI() {
        return new DialogueReactiveAI();
    }

    @Bean
    public PassiveAnimalAI passiveAnimalAI() {
        return new PassiveAnimalAI();
    }

    @Bean
    public AnimalBehaviorProfile animalBehaviorProfile() {
        return new AnimalBehaviorProfile();
    }
}

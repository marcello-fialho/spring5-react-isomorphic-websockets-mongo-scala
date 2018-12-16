package com.wonderprints.isomorphic.example.repositories.util

import com.wonderprints.isomorphic.example.model.VisibilityFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component


@Component
@Autowired
class VisibilityFilterRepositoryInitializer (
    private val mongoTemplate: MongoTemplate)
    extends CommandLineRunner {

  def run(args: String*): Unit = {
    mongoTemplate.dropCollection("visibilityFilter")
    mongoTemplate.insert(new VisibilityFilter("show_all"), "visibilityFilter")
  }
}


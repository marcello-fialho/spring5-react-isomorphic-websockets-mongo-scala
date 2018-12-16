package com.wonderprints.isomorphic.example.config

import com.mongodb.MongoClient
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.data.mongodb.config.AbstractMongoConfiguration

@Configuration
class MongoConfig extends AbstractMongoConfiguration{

  @Bean
  override def mongoClient(): MongoClient = new MongoClient("127.0.0.1", 27017)

  import org.springframework.context.annotation.Bean
  import org.springframework.data.mongodb.core.MongoTemplate

  @Bean
  @throws[Exception]
  override def mongoTemplate = new MongoTemplate(mongoClient(), "test")

  override def getDatabaseName = "test"
}

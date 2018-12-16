package com.wonderprints.isomorphic.example.repositories.util

import com.wonderprints.isomorphic.example.model.Todo
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.CommandLineRunner
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import org.springframework.data.mongodb.core.BulkOperations.BulkMode

import scala.collection.JavaConverters._

@Component
@Autowired
class TodoRepositoryInitializer(private val mongoTemplate: MongoTemplate) extends CommandLineRunner {
  @Value("${emptyDB:false}")
  var emptyDB: Boolean = _

  def run(args: String*): Unit = {
      mongoTemplate.dropCollection("todo")
      if (!emptyDB) {
        val ops = mongoTemplate.bulkOps(BulkMode.UNORDERED, classOf[Todo])
        val list = List(Todo("1", "Learn JavaScript", completed = false),
          Todo("2", "Learn React", completed = false),
          Todo("3", "Learn React Router", completed = false),
          Todo("4", "Learn Redux", completed = false),
          Todo("5", "Learn RxJS", completed = false)).toBuffer.asJava
        ops.insert(list)
        ops.execute()
      }
  }

}


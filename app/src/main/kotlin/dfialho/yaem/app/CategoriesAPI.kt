package dfialho.yaem.app

import dfialho.yaem.app.api.Category
import dfialho.yaem.app.api.SubCategory
import dfialho.yaem.app.controllers.CategoryController
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*

fun Route.categories(controller: CategoryController) = route("categories") {

    post {
        val category = call.validatedReceive<Category>()
        val createdCategory = controller.create(category)
        call.respond(HttpStatusCode.Created, createdCategory)
    }

    post("sub") {
        val subCategory = call.validatedReceive<SubCategory>()
        val createdSubCategory = controller.create(subCategory)
        call.respond(HttpStatusCode.Created, createdSubCategory)
    }

    get {
        val categories = controller.list()
        call.respond(HttpStatusCode.OK, categories)
    }

    get("{name}") {
        val name = call.parameters("name")
        val category = controller.get(name)
        call.respond(HttpStatusCode.OK, category)
    }

    put("{name}") {
        val oldName = call.parameters("name")
        val category = call.validatedReceive<Category>()
        val updatedCategory = controller.rename(oldName, category.name)
        call.respond(HttpStatusCode.Accepted, updatedCategory)
    }

    delete("{name}") {
        val name = call.parameters("name")
        controller.delete(name)
        call.respond(HttpStatusCode.Accepted)
    }

    delete("{category}/{name}") {
        val category = call.parameters("category")
        val subCategory = call.parameters("name")
        controller.delete(SubCategory(category, subCategory))
        call.respond(HttpStatusCode.Accepted)
    }
}

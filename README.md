## Custom column sorting demo
This project demonstrates several approaches how one can implement a sorting by a custom `dataGrid` column in an Entity List view. Launch this project to view all approaches at once, in separate menu items.

**Legend**: A custom "Last visit date" is added to the list of Pets. It displays the maximum `startDate` of all Visits associated with the Pet.

The sorting task gets complicated due to the fact that it overlaps with pagination aspect of the List views. End users expect that the data set will be "sorted" first, and only after that it will be "paginated". So if the sorting algorithm is implemented in a data store (using SQL query), then pagination can also be implemented in a data store. **But** if the sorting can't be implemented on a data store level, only on the application server level, in-memory - then pagination has to be either disabled, or also implemented in-memory (which in turn leads to obvious performance problems for large data sets).

The following approaches are demonstrated:
- Disabled sorting for the custom column (no sorting - no problem)
- In-memory sorting, pagination disabled
- In-memory sorting, in-memory pagination
- In-database sorting with SQL function, in-database pagination

Other possible approaches:
- Denormalizing a data model, creating a persistent attribute just for the purpose of efficient sorting.

Comparison:

| Name | Sorting | Pagination | Applicability                                                |
| ------------- | ------------- |------------|--------------------------------------------------------------|
| Disabled sorting | disabled | in DB      | Recommended unless sorting was explicitly requested          |
| In-memory sorting, pagination disabled | in memory | disabled   | Small data sets                                              |
| In-memory sorting, in-memory pagination | in memory | in memory  | Small data sets. Inefficient.                                |
| In-database sorting with SQL function | in DB | in DB      | A SQL function for the sort weight is possible to implement. |

### Disabled sorting
Menu item: **Pets (no sort)**.

Related files:
 - src/main/resources/io/jmix/petclinic/view/pet/pet/pet-list-view-no-sort.xml

No sorting - no problem.

Consider to disable sorting for all custom columns unless a proper sorting logic has been implemented for them.

### In-memory sorting, pagination disabled
Menu item: **Pets (no paging, in memory)**

Related files:
- src/main/resources/io/jmix/petclinic/view/pet/pet/pet-list-view-no-paging-memory-sort.xml
- io.jmix.petclinic.view.pet.pet.PetListViewNoPagingMemorySort

The whole data set is loaded into memory. Then it is sorted in Java code.

Consider to use in-memory sorting and disable paging in the List view if you know that the displayed data set will always stay small (< 500 rows), so no pagination is actually required in the UI.

### In-memory sorting, in-memory pagination
Menu item: **Pets (paging, in memory)**

Related files:
- src/main/resources/io/jmix/petclinic/view/pet/pet/pet-list-view-memory-paging-memory-sort.xml
- io.jmix.petclinic.view.pet.pet.PetListViewMemoryPagingMemorySort

The whole data set is loaded into memory. Then it is sorted in Java code. Finally, a small page is extracted and shown in UI.

Consider this method as a last resort for small data sets (< 5000 rows), if creating a SQL function (see below) is impossible. Memory-inefficient.

### In-database sorting with SQL function
Menu item: **Pets (paging, SQL function)**

Related files:
- src/main/resources/io/jmix/petclinic/view/pet/pet/pet-list-view-db-paging-db-function-sort.xml
- io.jmix.petclinic.view.pet.pet.PetListViewDbPagingDbFunctionSort
- src/main/resources/io/jmix/petclinic/liquibase/changelog/2025/03/18-last-visit-date-sort-function.xml - creation of SQL function

JPQL query transforming is used. In the data load delegate, the custom "order by custom_function_name" clause is added to the JPQL query string. Sorting as well as pagination are performed efficiently in the database. 

This method is efficient for large data sets.

Instead of using SQL function, one can transform the JPQL query string in other way, e.g. by adding an order by clause by some existing persistent attribute.

## Jmix Petclinic

![](src/main/resources/META-INF/resources/images/petclinic_logo_with_slogan.svg)

Jmix Petclinic is an example application built with Jmix framework. It is based on the commonly known [Spring Petclinic](https://github.com/spring-projects/spring-petclinic) example.

## Online Demo

The Jmix Petclinic application is available online at https://demo.jmix.io/petclinic

## Application Overview

Jmix Petclinic provides the following functionality:

- Managing Pet Visits through a Calendar
- Tracking Visit Treatments for Nurses
- Creating Pets and Owners
- Managing Nurses and Veterinarians of the Petclinic

## Domain Model

![](etc/domain-model.png)
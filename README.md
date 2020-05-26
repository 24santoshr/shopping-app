# shopping-app
Shopping app service models a shopping website and provides backend REST interfaces open for interaction.

The Shopping app component provides a basic operation capabilities which include adding user for website, adding products for the website, adding items to shopping card and checkout functionalities. By default, the REST interface is exposed at *0.0.0.0:9000*, and contains endpoints for: 
                    
* Adding users to the website
* Adding products to the website
* Adding and Viewing items in the shoppingcard
* Checkout

           
  Use `sbt test` to test the endpoints.
  
## Assumptions
The model has been divied into two parts Shopping Website Management and Shopping Card Management. While anyone can add users and products to Website, only added users can perform shop functionalities such has adding items to card, viewing and checking out.  

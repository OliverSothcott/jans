# Agama

Agama is an auth-server component that offers an alternative way to build authentication flows in Janssen server.
Originally, person authentication flows are defined in the server by means of jython scripts that adhere to a predefined API. With Agama, flows are coded in a DSL (domain specific language) designed for the sole purpose of writing web flows. 

Some of the advantages of using Agama include:

1. Ability to express authentication flows in a clean and concise way
1. Flow composition is supported out-of-the-box: reuse of an existing flow in another requires no effort
1. Reasoning about flows behavior is straightforward (as consequence of points 1 and 2). This makes flow modifications easy
1. Small cognitive load. Agama DSL is a very small language with simple, non-distracting syntax
1. Friendly UI templating engine. No complexities when authoring web pages - stay focused on writing HTML markup

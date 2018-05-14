# fy-stock interview task

Purpose of the task is to create an idea of solution. Not to code solution itself.

Company Fy! has e-store to combine products from suppliers in one place. Suppliers work with Fy! on a dropshipping model.

System is stock preview for suppliers and Fy!. Intention of that system is to let supplier and Fy! track stock and availability.

- Products are located in suppliers warehouses
- Suppliers send products themselves (dropship model)
- Fy! use centralised database for users data

## Functions of the system

Using the system, brands should be able to:
- Log in with Google, Facebook or similar
- Record when they have received new stock for a product
- Record when they have shipped stock to a customer
- Record when they have received a return from a customer
- See the current available stock level for each product
- Be notified via email when they are out of stock of a product
- Notify Fy! whenever a stock level changes, identify product by SKU code

## Deployment

- Deploy by Continuous Integration / Deployment using Docker. Kubernetes / Google Cloud / AWS or any other solution compatible with Docker which is used by Fy!
- Application can be run parallel, so if any instance will crash users will automatically use different one without crash experience.
- Personally my favourite free CI/CD tool is https://concourse-ci.org, but it could be popular Jenkins or any other solution used by Fy!
- Tests will run in Dockerfile by CI/CD and locally by developers in the same way. There is also no risk to deploy version with failed tests, because image wouldn't build.

## Scaling in the future

- Solution based on Docker. It is easy to run application parallel on the same machine or multiple machines.
- Database can be Datomic or Eventstore which are easy to scale up. Also using SQL database .performance can be improved on infrastructure side.
- System itself can be improved by clojure.async.
- Onyx can be added to distribute computation on software side.

## Possible improves in the future

- Items selling in different units than pieces, for example kg.
- Items can be under zero stock. Production on demand. For example custom products with dedication or limited expiration date.
- Semi-finished products for other artists. Process of completation new products from semi-finished products.
- Suppliers can have multiple accounts with access to stock

## Business improves suggestions

Because of dropship model, Fy! will have issues about quality of sending packages. Fy! should create standard of packaging process including:

- How to pack items (glass, fluid, loose products, organic)
- What can be include in boxes (advertisements)
- Sell special boxes and tapes to suppliers with Fy! logo
- System for suppliers to send items (courier labels, list of products to send). This should be connected with clients notification.
- System to manage complains about packages (package wasn't deliver on time etc.)
- Educate suppliers how perfect box should be made. Selling is about emotions during whole buying process and after that. Products are only addition which satisfy this emotions. Every mistake during sending process destroy clients experience.

# Worth to see

## Dockerfile

**How to boost deployment / testing time using docker multi-stage building.**

This is important, because developers will have to waste 15 minutes to build docker image. 15 minutes is an example, but depend on number of dependencies and app itself it could be much longer.
In consequence:
- They wouldn't build it locally and will push changes to CI/CD without proper tests. Bugs will be discovered on CI/CD step.
- They will have to wait 15 minutes to get result of build and tests
- Time to fix critical bugs on production will take more
- Deploy to production less, because of more effort
- Rare deployment means fear of changes and even more consequences

Summarizing this up: time of build, deploy and full tests is important.

**Avoid mistake to deploy system without green tests**

Personally I solve it doing tests in Dockerfile instead of run them as command in CI/CD tool directly. Then I am 100% sure service pass all tests. Otherwise image wouldn't build. It also let me keep service independen from CI/CD tool and easy move to different environment.

## Logs on production

My favourite tool for logs tracking on production in `Sentry`. Besides of good design, it is SaaS and open-source. It is easy to move it to inner servers when law demand it without additional effort.

 My favourite Clojure logger is `timbre`. From developer point of view it is important to track logs as simple and easy as possible. The best without any additional effort. For that purpose I use appenders in timbre to add Sentry. In file `src/fy_stock/logs.clj` we can see how easy is add tool like Sentry. Developers don't have to do any changes in code and even think about using Sentry during coding.

## Architecture of service

Personally I like to do assumption about folders tree:

```
├── src
│   └── fy_stock
│       ├── db
│       ├── ├── db.clj
│       │   ├── products.clj
│       │   └── stock.clj
│       ├── services
│       │   ├── email.clj
│       │   ├── fb.clj
│       │   ├── fy_users.clj
│       │   └── google.clj
│       ├── spec
│       │   ├── products.clj
│       │   ├── stock.clj
│       │   └── users.clj
│       ├── core.clj
│       ├── logs.clj
│       ├── products.clj
│       ├── stock.clj
│       └── users.clj
```

`/spec` - `clojure.spec` files
`/db` - queries to database
`/` - I keep main files as bussiness needs description. It means in this files I don't code technical realisations of intentions. I code this files as the best description of intentions of business needs as possible. The most important here is to explain why and how we are doing things instead of use less lines. This files are only ones where business logic is coded.
`/serivces` - here I keep technical realisation of business needs. From the beginning I think about this files as independent immature modules. When they became more complex they envolve to separate folders (for example `/fy-warehouse` with files `soap.clj`, `api.clj`, `parse.clj`). Finally when they became mature and common can be moved as separate repository.

For example if we have `/product.clj` I would code there what business let as to do with products: `add-product`, `disable-product`, `products->csv` etc. with all rules defined by business, but not code to generate csv itself.

**This solution let me do clear separation for business needs and technical realisation of intentions and easy make modules when code became mature. But what is even more importnat it lets me delay decisions. I can decide later which DB I will use, notify users by e-mail, sms or third party service by API and I can change my mind without fear of changes.**
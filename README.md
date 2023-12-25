# customsnowflakesequencekt
Customizable 64-bit Sequence ID Generator built in Kotlin compatible/based on Twitter's Snowflake

## Compile

```sh
mvn compile
mvn package
```

## Run

```sh
java -jar ./target/customsnowflakesequencekt-0.1.0.jar
```

Expected output:

```txt
Got quantity: '1'
Got node-id-bits: '10'
Got sequence-bits: '12'
Got custom-epoch: '1788921856'
Got node-id: '0'
Got unused-bits: '0'
Got micros-ten-power: '3'
Got cooldown: '1500'
```

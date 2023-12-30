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

Expected output (defaults):

```txt
Got quantity: '1'
Got unused-bits: '0'
Got node-id-bits: '10'
Got sequence-bits: '12'
Got timestamp bits (derived): '42'
Got custom-epoch: '2023-01-01T00:00:00Z'
Got node-id: '0'
Got micros-ten-power: '3'
Got cooldown: '500'
```

Customizing default values:

```sh
java -jar target/customsnowflakesequencekt-0.1.0.jar --sequence-bits 13 --quantity 3 --node-id-bits 9 --unused-bits 1 --custom-epoch '2022-01-01T00:00:00Z' --node-id 22 --micros-ten-power 2 --cooldown 200
```

Output

```txt
Got quantity: '3'
Got unused-bits: '1'
Got node-id-bits: '9'
Got sequence-bits: '13'
Got timestamp bits (derived): '41'
Got custom-epoch: '2022-01-01T00:00:00Z'
Got node-id: '22'
Got micros-ten-power: '2'
Got cooldown: '200'
```
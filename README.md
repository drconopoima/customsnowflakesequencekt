# customsnowflakesequencekt

Customizable 64-bit Sequence ID Generator built in Kotlin compatible/based on Twitter's Snowflake

## Compile

```sh
mvn compile
mvn package
```

## Run

By default, it will use default values detailed in example below, and output an ID per line per the --quantity parameter 

```sh
java -jar ./target/customsnowflakesequencekt-0.1.0.jar --quantity 6
133985301784690688
133985301784691712
133985301784692736
133985301784693760
133985301784694784
133985301784695808
```

It can output verbose output for each of the default/parsed parameters for the snowflake-id if you provide "--debug" argument.
It can also output ID generation time information in order for helping customizing optimal values for a particular platform, use "--time" parameter.

```sh
java -jar ./target/customsnowflakesequencekt-0.1.0.jar --time --debug
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
Got max-sequence: '4095'
133984577067679744
It took 1522872 nanoseconds, time per id: 1522872.0 ns
```

Customizing default values:

```sh
java -jar target/customsnowflakesequencekt-0.1.0.jar --sequence-bits 13 --quantity 1000000 --node-id-bits 9 --unused-bits 1 --custom-epoch '2024-01-01T00:00:00Z' --node-id 22 --micros-ten-power 2 --debug --time
```

Output

```txt
Got quantity: '3'
Got unused-bits: '1'
Got node-id-bits: '9'
Got sequence-bits: '13'
Got timestamp bits (derived): '41'
Got custom-epoch: '2024-01-01T00:00:00Z'
Got node-id: '22'
Got micros-ten-power: '0'
Got max-sequence: '8191'
1254566682624
1254566683136
It took 82581222 nanoseconds, time per id: 82.581222 ns
```

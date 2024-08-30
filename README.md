

# Java Implementation for 10 Arm Upper Confidence Bound Problem

- Having understood the 10 Arm Bandit problem (https://github.com/hsty/10-arm-bandit) I wanted to implement the UCB method.

- In addition to the RLBook (http://incompleteideas.net/book/RLbook2020.pdf), this paper helped me a lot (https://people.maths.bris.ac.uk/~maajg/teaching/stochopt/ucb.pdf)

- I chose to seperate the processing of dataseries into two different function to separate concerns and clear implementation. 
  As earlier, the primary motivation is to understand the 10 ARM UCB method better.

- Performance is not the primary motivation, understanding is, hence
  there are many places where optimization can be done but hasnt been implemented.

- Speaking of performance, the program
  runs in about 90 seconds on my laptop.

- As can be seen **u=2** outperforms **ε = 0.1** over many steps. You will also notice the initial spike because all values are closer to each other.

# Output

![Output](https://github.com/hsty/10-arm-ucb/blob/main/output.png)

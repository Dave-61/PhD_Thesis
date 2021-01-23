__Analytical Approaches to Risk Assessment and Management for Railroad Transportation of Hazardous Materials (2018)__

Rail hazmat accidents rarely happen (*low-probability* incidents), but if they do occur, then the consequences can be disastrous (*high-consequence* incidents), reflecting on both the population and the environment. Therefore, it is critical to make a *risk-averse* route decision in rail hazmat transportation. Existing risk assessment methodologies� efficacy, however, has been limited as they either are risk-neutral, which fail to capture the public posture against hazmat transportation, or result in a unique optimal route, regardless of the risk preference of decision makers. I have made the first attempt to fill this gap by building on the risk measures developed in finance to manage losses of portfolios, i.e., *Value-at-Risk (VaR)* and *Conditional Value-at-Risk (CVaR)* models, and then adapted them to mitigate the risk associated with hazmat transportation in railroad networks by producing a more *flexible* and *reliable* route modeling approach. Instead of a single optimal route output, these models have a two-dimensional framework which produce *alternative route choices* given different confidence levels. In addition, while most existing hazmat routing methods study the entire risk distribution, these new models, especially *CVaR*, focus more on the long tail of the risk distribution to avoid extreme events (catastrophic rail hazmat accidents). To that end, freight train derailment data of the United States Federal Railroad Administration (FRA: 1995-2009) have been statistically analyzed to develop expressions that would incorporate characteristics of railroad accidents, and then to estimate the different inputs to the models. To validate the proposed methodologies and demonstrate their significant contribution to the existing body of literature, they have been utilized to study a case study generated using the realistic infrastructure of a railroad network in Midwest United States. This study has been published in [Transportation Research Part D: Transport and Environment](https://www.sciencedirect.com/science/article/pii/S1361920917301797) (2017), and [Transportation Research Part B: Methodological](https://www.sciencedirect.com/science/article/pii/S0191261517303545) (2018). <br>

As the last piece of my PhD dissertation, I extended the proposed CVaR methodology developed earlier for a single rail hazmat shipment, single origin-destination pair, to *multiple* rail hazmat shipments. This aspect leads to a harder class of problems that involve *multi-commodity* and *multiple* origin-destination hazmat routing decisions. On the other hand, it may happen that certain links and yards of the railroad network tend to be *overloaded* with hazmat traffic and risk. This becomes crucial when certain populated zones are exposed to an intolerable level of risk resulting from the routing decisions. To overcome this issue, I also promote *equity* in the spatial distribution of risk throughout the railroad network. Therefore, the main problem is to find minimum risk routes, as measured by CVaR methodology, while limiting and equitably spreading the risk in any zone where the railroad network is embedded. This work has been published in [Computers & Operations Research](https://www.sciencedirect.com/science/article/abs/pii/S0305054821000149) (2020). <br>

This repository contains the following **algorithms** which have been developed to solve the proposed **(stochastic) integer programming** models:
- Algorithms to generate the following matrixes from raw data:
	- Accident probability 
	- Accident consequence 
	- Distance between yards
	- Train services (routes)
	- Hazmat shipmen volumes between yards
- Conditional value-at-risk (CVaR) algorithm
- Tailored shortest path algorithms based on Dijkstra�s algorithm
- Lagrangian relaxation method
- Subgradient optimization algorithm
- A greedy heuristic algorithm (for k-minimal CVaR paths)
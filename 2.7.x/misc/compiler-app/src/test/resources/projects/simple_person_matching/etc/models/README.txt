2015-04-01 rphall

Because of a bug in the ChoiceMaker DB blocking code generator, some versions
of the Person and Person2 model have a field named 'recordId' whereas others
have a field named 'record_id'. See CMA-7 on the ChoiceMaker JIRA website
(currently https://choicemaker.atlassian.net/browse/CMA-7) for more details.

The plan is that code examples and model plugin are written to workaround the
bug, so these models have a field named 'record_id'. In contrast, unit tests
and integration tests of the compiler and maven plugin are written to
illustrate the bug, so these models are have a field named 'recordId'. In
all models, the corresponding database column is named 'record_id'.


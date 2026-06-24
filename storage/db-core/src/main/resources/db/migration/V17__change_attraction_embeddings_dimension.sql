-- Drop existing constraint and alter embedding vector column size to 1536 for text-embedding-3-small
truncate table attraction_embeddings;

alter table attraction_embeddings drop constraint chk_attraction_embeddings_dimension;

alter table attraction_embeddings alter column embedding type vector(1536);

alter table attraction_embeddings add constraint chk_attraction_embeddings_dimension check (embedding_dimension = 1536);

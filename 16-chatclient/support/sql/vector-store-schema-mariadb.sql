-- mydb.vector_store definition

CREATE TABLE `vector_store` (
                                `id` uuid NOT NULL DEFAULT uuid(),
                                `content` text DEFAULT NULL,
                                `metadata` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`metadata`)),
                                `embedding` vector(1536) NOT NULL,
                                PRIMARY KEY (`id`),
                                VECTOR KEY `vector_store_embedding_idx` (`embedding`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
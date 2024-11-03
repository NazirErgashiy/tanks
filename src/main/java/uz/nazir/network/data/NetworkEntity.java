package uz.nazir.network.data;

import uz.nazir.game.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NetworkEntity {
    private UUID id;
    private DataCall dataCall;
    private Vector2 position;
    private Direction direction;
    private EntityType entityType;
    private boolean isOwner;
    private int[][] map;
    private UUID ownerId;
}

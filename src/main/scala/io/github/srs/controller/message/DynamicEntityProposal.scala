package io.github.srs.controller.message

import cats.effect.IO
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.DynamicEntity

/**
 * Represents a proposal for a dynamic entity to perform an action.
 *
 * @param entity
 *   the entity that is being proposed to perform the action.
 * @param action
 *   the action that the entity is proposed to perform.
 */
final case class DynamicEntityProposal(entity: DynamicEntity, action: Action[IO])

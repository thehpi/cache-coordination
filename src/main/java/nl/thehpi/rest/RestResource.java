package nl.thehpi.rest;

import nl.thehpi.entities.World;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Path("hello")
@Stateless
public class RestResource {

  @PersistenceContext
  private EntityManager em;

  @Path("world")
  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public List<World> getAll(World world)
  {
    return this.em.createQuery("select a from World a", World.class).getResultList();
  }

  @Path("world")
  @POST
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response createWorld(World world)
  {
    world.setId(UUID.randomUUID().toString());
    this.em.persist(world);

    return Response.ok().entity(world).build();
  }

  @Path("world/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response getWorld(@PathParam("id") String id) {
    World world = this.em.find(World.class, id);
    if (world == null) {
      throw new NotFoundException();
    }
    return Response.ok().entity(this.em.find(World.class, id)).build();
  }

  @Path("world/{id}/{field1}")
  @PUT
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public World update(@PathParam("id") String id, @PathParam("field1") String field1) {
    World h = this.em.find(World.class,id);
    h.setField1(field1);
    return h;
  }
}
